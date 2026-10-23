/**
 * SafeCircle Cloud Functions (Node.js 18)
 * 1. onSosAlertCreated: High-priority FCM push fan-out to all group members when an SOS is triggered
 * 2. validateAndJoinGroup: Server-side join-code lookup, 20-member cap enforcement, and approval handling
 * 3. notifyGeofenceTransition: Push notification to group members when a user enters/exits a Safe Zone
 */

const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.database();
const messaging = admin.messaging();

const MAX_GROUP_MEMBERS = 20;
const SOS_RATE_LIMIT_MS = 15000; // Minimum 15s between new SOS alerts from the same user

/**
 * 1. Triggered when a new SOS alert is written to /alerts/{gid}/{aid}
 * Fetches all group members' FCM tokens and broadcasts a high-priority data payload
 * that wakes devices on lock screen via IncomingAlertActivity.
 */
exports.onSosAlertCreated = functions.database
    .ref("/alerts/{gid}/{aid}")
    .onCreate(async (snapshot, context) => {
      const {gid, aid} = context.params;
      const alertData = snapshot.val();
      if (!alertData || !alertData.senderUid) {
        return null;
      }

      const senderUid = alertData.senderUid;

      // Rate-limiting check on user's last SOS timestamp
      const lastSosRef = db.ref(`/users/${senderUid}/lastSosTimestamp`);
      const lastSosSnap = await lastSosRef.once("value");
      const now = Date.now();
      if (lastSosSnap.exists() && (now - lastSosSnap.val()) < SOS_RATE_LIMIT_MS) {
        console.warn(`Rate limit exceeded for user ${senderUid} in group ${gid}`);
        return null;
      }
      await lastSosRef.set(now);

      // Fetch sender profile & group details in parallel
      const [senderSnap, groupSnap] = await Promise.all([
        db.ref(`/users/${senderUid}`).once("value"),
        db.ref(`/groups/${gid}`).once("value"),
      ]);

      const sender = senderSnap.val() || {};
      const group = groupSnap.val() || {};
      const members = group.members || {};

      const recipientUids = Object.keys(members).filter((uid) => uid !== senderUid);
      if (recipientUids.length === 0) {
        return null;
      }

      // Fetch FCM tokens for all recipient members
      const tokenPromises = recipientUids.map((uid) =>
        db.ref(`/users/${uid}/fcmToken`).once("value")
      );
      const tokenSnapshots = await Promise.all(tokenPromises);
      const tokens = tokenSnapshots
          .map((snap) => snap.val())
          .filter((token) => typeof token === "string" && token.length > 0);

      if (tokens.length === 0) {
        return null;
      }

      const payload = {
        tokens: tokens,
        data: {
          type: "SOS_ALERT",
          alertId: String(aid),
          groupId: String(gid),
          groupName: String(group.name || "SafeCircle"),
          senderUid: String(senderUid),
          senderName: String(sender.name || "Group Member"),
          senderPhone: String(sender.phone || ""),
          lat: String(alertData.lat || 0),
          lng: String(alertData.lng || 0),
          timestamp: String(alertData.timestamp || now),
          audioUrl: String(alertData.audioUrl || ""),
        },
        android: {
          priority: "high",
          ttl: 3600 * 1000,
        },
      };

      const response = await messaging.sendEachForMulticast(payload);
      console.log(
          `SOS Fan-out for alert ${aid} in group ${gid}: ` +
          `${response.successCount} sent, ${response.failureCount} failed.`
      );
      return null;
    });

/**
 * 2. Callable Function: Validates 6-character join code, checks 20-member cap,
 * and adds user to either /groups/{gid}/members or /groups/{gid}/pendingMembers.
 */
exports.validateAndJoinGroup = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError(
        "unauthenticated",
        "You must be signed in to join a group."
    );
  }

  const uid = context.auth.uid;
  const rawCode = (data.code || "").toString().trim().toUpperCase();

  if (!/^[A-Z0-9]{6}$/.test(rawCode)) {
    throw new functions.https.HttpsError(
        "invalid-argument",
        "Join code must be exactly 6 alphanumeric characters."
    );
  }

  const codeSnap = await db.ref(`/joinCodes/${rawCode}`).once("value");
  if (!codeSnap.exists()) {
    throw new functions.https.HttpsError(
        "not-found",
        "Invalid or expired join code."
    );
  }

  const gid = codeSnap.val();
  const groupSnap = await db.ref(`/groups/${gid}`).once("value");
  if (!groupSnap.exists()) {
    throw new functions.https.HttpsError(
        "not-found",
        "The group associated with this code no longer exists."
    );
  }

  const group = groupSnap.val();
  const members = group.members || {};

  // Check if already a member
  if (members[uid]) {
    return {status: "ALREADY_MEMBER", groupId: gid, groupName: group.name};
  }

  // Enforce 20-member cap
  if (Object.keys(members).length >= MAX_GROUP_MEMBERS) {
    throw new functions.https.HttpsError(
        "resource-exhausted",
        "This group has reached the maximum limit of 20 members."
    );
  }

  // Check if admin approval is required
  if (group.requireApproval === true) {
    await db.ref(`/groups/${gid}/pendingMembers/${uid}`).set(Date.now());
    return {status: "PENDING_APPROVAL", groupId: gid, groupName: group.name};
  }

  // Instant join: atomically add member to group and group to user's groupIds
  const updates = {};
  updates[`/groups/${gid}/members/${uid}`] = "member";
  updates[`/users/${uid}/groupIds/${gid}`] = true;
  await db.ref().update(updates);

  return {status: "JOINED", groupId: gid, groupName: group.name};
});

/**
 * 3. Callable Function: Fan-out Geofence enter/exit notification to group members
 */
exports.notifyGeofenceTransition = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Authentication required.");
  }

  const uid = context.auth.uid;
  const {groupId, zoneName, transitionType, lat, lng} = data;
  if (!groupId || !zoneName || !transitionType) {
    throw new functions.https.HttpsError("invalid-argument", "Missing geofence parameters.");
  }

  const [userSnap, groupSnap] = await Promise.all([
    db.ref(`/users/${uid}`).once("value"),
    db.ref(`/groups/${groupId}`).once("value"),
  ]);

  const user = userSnap.val() || {};
  const group = groupSnap.val() || {};
  const members = group.members || {};

  if (!members[uid]) {
    throw new functions.https.HttpsError("permission-denied", "Not a member of this group.");
  }

  const recipientUids = Object.keys(members).filter((id) => id !== uid);
  const tokenSnapshots = await Promise.all(
      recipientUids.map((id) => db.ref(`/users/${id}/fcmToken`).once("value"))
  );
  const tokens = tokenSnapshots
      .map((s) => s.val())
      .filter((t) => typeof t === "string" && t.length > 0);

  if (tokens.length === 0) {
    return {sent: 0};
  }

  const payload = {
    tokens: tokens,
    data: {
      type: "GEOFENCE_ALERT",
      groupId: String(groupId),
      groupName: String(group.name || "SafeCircle"),
      senderUid: String(uid),
      senderName: String(user.name || "Member"),
      zoneName: String(zoneName),
      transitionType: String(transitionType), // "ENTER" or "EXIT"
      lat: String(lat || 0),
      lng: String(lng || 0),
      timestamp: String(Date.now()),
    },
    android: {
      priority: "high",
    },
  };

  const res = await messaging.sendEachForMulticast(payload);
  return {sent: res.successCount};
});

