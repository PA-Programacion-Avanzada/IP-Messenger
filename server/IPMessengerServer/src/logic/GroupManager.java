package logic;

import database.FriendshipDAO;
import database.GroupDAO;
import database.GroupMemberDAO;
import database.UserDAO;
import models.Group;
import models.User;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupManager {
    private GroupDAO groupDAO = new GroupDAO();
    private GroupMemberDAO memberDAO = new GroupMemberDAO();
    private UserDAO userDAO = new UserDAO();

    public int createGroup(String name, int creatorId, List<Integer> invitedUserIds) throws SQLException {
        // El grupo debe tener al menos un amigo invitado para que el creador pueda comenzar
        if (invitedUserIds == null || invitedUserIds.isEmpty()) return -1;

        FriendshipDAO friendshipDAO = new FriendshipDAO();
        for (int uid : invitedUserIds) {
            if (!friendshipDAO.areFriends(creatorId, uid)) {
                return -1;
            }
        }

        int groupId = groupDAO.createGroup(name, creatorId);
        if (groupId == -1) return -1;

        // El creador se agrega como accepted
        memberDAO.inviteMember(groupId, creatorId);
        memberDAO.updateStatus(groupId, creatorId, "accepted");

        // Agregar a los demás miembros directamente como aceptados
        for (int uid : invitedUserIds) {
            memberDAO.inviteMember(groupId, uid);
            memberDAO.updateStatus(groupId, uid, "accepted");
        }
        return groupId;
    }

    public void acceptInvitation(int groupId, int userId) throws SQLException {
        memberDAO.updateStatus(groupId, userId, "accepted");
        // Verificar si después de esta aceptación el grupo tiene al menos 3 miembros activos
        int activeCount = memberDAO.countAcceptedMembers(groupId);
        if (activeCount >= 3) {
            // El grupo se activa (puedes notificar a los miembros, o simplemente dejar que funcione)
            // No se requiere acción extra.
        }
    }

    public void rejectInvitation(int groupId, int userId) throws SQLException {
        memberDAO.updateStatus(groupId, userId, "rejected");
        // Verificar si después del rechazo el grupo se queda con menos de 3 miembros activos
        int activeCount = memberDAO.countAcceptedMembers(groupId);
        if (activeCount < 3) {
            // Eliminar el grupo
            groupDAO.deleteGroup(groupId);
        }
    }

    public void leaveGroup(int groupId, int userId) throws SQLException {
        memberDAO.deleteMember(groupId, userId);
        int activeCount = memberDAO.countAcceptedMembers(groupId);
        if (activeCount < 3) {
            groupDAO.deleteGroup(groupId);
        }
    }

    public List<Group> getGroupsForUser(int userId) throws SQLException {
        List<Integer> groupIds = memberDAO.getAcceptedGroupIds(userId);
        List<Group> groups = new ArrayList<>();
        for (int groupId : groupIds) {
            Group group = groupDAO.findById(groupId);
            if (group != null) {
                groups.add(group);
            }
        }
        return groups;
    }

    public List<User> getGroupMembers(int groupId) throws SQLException {
        List<Integer> memberIds = memberDAO.getAcceptedMemberIds(groupId);
        List<User> members = new ArrayList<>();
        for (int id : memberIds) {
            User u = userDAO.findById(id);
            if (u != null) members.add(u);
        }
        return members;
    }

    public int inviteMembersToGroup(int groupId, int inviterId, List<Integer> invitedUserIds) throws SQLException {
        if (!memberDAO.isAcceptedMember(groupId, inviterId)) {
            return 0;
        }
        if (groupDAO.findById(groupId) == null) {
            return 0;
        }

        FriendshipDAO friendshipDAO = new FriendshipDAO();
        int added = 0;
        for (int uid : invitedUserIds) {
            if (uid == inviterId) {
                continue;
            }
            if (!friendshipDAO.areFriends(inviterId, uid)) {
                continue;
            }
            String status = memberDAO.getMemberStatus(groupId, uid);
            if ("accepted".equals(status) || "invited".equals(status)) {
                continue;
            }
            memberDAO.reinviteMember(groupId, uid);
            added++;
        }
        return added;
    }

    public List<Map<String, Object>> getPendingGroupInvites(int userId) throws SQLException {
        List<Map<String, Object>> invites = new ArrayList<>();
        for (int groupId : memberDAO.getInvitedGroupIds(userId)) {
            Group group = groupDAO.findById(groupId);
            if (group == null) {
                continue;
            }
            User creator = userDAO.findById(group.getCreatorId());
            Map<String, Object> invite = new HashMap<>();
            invite.put("groupId", groupId);
            invite.put("groupName", group.getName());
            invite.put("inviterId", group.getCreatorId());
            invite.put("inviterName", creator != null ? creator.getUsername() : "Usuario");
            invites.add(invite);
        }
        return invites;
    }
}