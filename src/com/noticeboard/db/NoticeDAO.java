package com.noticeboard.db;

import com.noticeboard.model.Notice;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoticeDAO {

    public boolean addNotice(Notice notice) throws SQLException {
        String sql = "INSERT INTO notices (title, content, category, priority, attachment_name, attachment_data, attachment_type, posted_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, notice.getTitle());
            ps.setString(2, notice.getContent());
            ps.setString(3, notice.getCategory());
            ps.setString(4, notice.getPriority());
            ps.setString(5, notice.getAttachmentName());
            if (notice.getAttachmentData() != null) {
                ps.setBytes(6, notice.getAttachmentData());
            } else {
                ps.setNull(6, Types.BLOB);
            }
            ps.setString(7, notice.getAttachmentType());
            ps.setInt(8, notice.getPostedBy());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateNotice(Notice notice) throws SQLException {
        String sql = "UPDATE notices SET title=?, content=?, category=?, priority=?, attachment_name=?, attachment_data=?, attachment_type=?, updated_at=NOW() WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, notice.getTitle());
            ps.setString(2, notice.getContent());
            ps.setString(3, notice.getCategory());
            ps.setString(4, notice.getPriority());
            ps.setString(5, notice.getAttachmentName());
            if (notice.getAttachmentData() != null) {
                ps.setBytes(6, notice.getAttachmentData());
            } else {
                ps.setNull(6, Types.BLOB);
            }
            ps.setString(7, notice.getAttachmentType());
            ps.setInt(8, notice.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteNotice(int id) throws SQLException {
        String sql = "DELETE FROM notices WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Notice> getAllNotices() throws SQLException {
        List<Notice> list = new ArrayList<>();
        String sql = "SELECT n.*, u.name AS posted_by_name FROM notices n LEFT JOIN users u ON n.posted_by = u.id ORDER BY n.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapNotice(rs, false));
            }
        }
        return list;
    }

    public List<Notice> searchNotices(String keyword) throws SQLException {
        List<Notice> list = new ArrayList<>();
        String sql = "SELECT n.*, u.name AS posted_by_name FROM notices n LEFT JOIN users u ON n.posted_by = u.id WHERE n.title LIKE ? OR n.content LIKE ? ORDER BY n.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String kw = "%" + keyword + "%";
            ps.setString(1, kw);
            ps.setString(2, kw);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapNotice(rs, false));
            }
        }
        return list;
    }

    public List<Notice> filterByCategory(String category) throws SQLException {
        List<Notice> list = new ArrayList<>();
        String sql = "SELECT n.*, u.name AS posted_by_name FROM notices n LEFT JOIN users u ON n.posted_by = u.id WHERE n.category = ? ORDER BY n.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapNotice(rs, false));
            }
        }
        return list;
    }

    public Notice getNoticeWithAttachment(int id) throws SQLException {
        String sql = "SELECT n.*, u.name AS posted_by_name FROM notices n LEFT JOIN users u ON n.posted_by = u.id WHERE n.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapNotice(rs, true);
            }
        }
        return null;
    }

    private Notice mapNotice(ResultSet rs, boolean includeBlob) throws SQLException {
        Notice n = new Notice();
        n.setId(rs.getInt("id"));
        n.setTitle(rs.getString("title"));
        n.setContent(rs.getString("content"));
        n.setCategory(rs.getString("category"));
        n.setPriority(rs.getString("priority"));
        n.setAttachmentName(rs.getString("attachment_name"));
        n.setAttachmentType(rs.getString("attachment_type"));
        if (includeBlob) {
            n.setAttachmentData(rs.getBytes("attachment_data"));
        }
        n.setPostedBy(rs.getInt("posted_by"));
        n.setPostedByName(rs.getString("posted_by_name"));
        n.setCreatedAt(rs.getTimestamp("created_at"));
        n.setUpdatedAt(rs.getTimestamp("updated_at"));
        return n;
    }
}
