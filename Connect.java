package com.example.sharepictures;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Connect {
    private static Connection conn = null;

    public static Connection getConnection(String dbName) throws SQLException {
        try {
            if (conn == null || conn.isClosed()) {
                Class.forName("com.mysql.jdbc.Driver"); // 加载驱动
                String ip = "rm-cn-x0r3etfb100032ro.rwlb.rds.aliyuncs.com";// 填入你的公网ip地址

                conn = DriverManager.getConnection(
                        "jdbc:mysql://" + ip + ":3306/" + dbName + "?useSSL=false",
                        "share001", "Share001");
            }
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return conn; // 返回Connection型变量conn用于后续连接
    }

    //关闭连接
    public static void closeConnection() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
            conn = null;
        }
    }

    //注册
    public static void insertUser(String id, String password, byte[] touxiang) throws SQLException {
        try {
            getConnection("001for");
            //增 user
            String sql = "INSERT INTO users (id, password, touxiang) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, password);
            preparedStatement.setBytes(3, touxiang);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    //插入图片
    public static int insertPicture(String idnum, String details, int likes, byte[] pictures, String uploader) throws SQLException {
        int value = 0;
        try {
            if (conn == null || conn.isClosed()) {
                conn = getConnection("001for");
            }
            // 检查是否已存在相同的图片名称
            String checkDuplicateSql = "SELECT COUNT(*) FROM picturestable WHERE idnum = ?";
            PreparedStatement checkDuplicateStatement = conn.prepareStatement(checkDuplicateSql);
            checkDuplicateStatement.setString(1, idnum);
            ResultSet resultSet = checkDuplicateStatement.executeQuery();
            resultSet.next();
            int count = resultSet.getInt(1);

            if (count == 0) {
                // 如果图片名称不重复，则插入记录
                String insertSql = "INSERT INTO picturestable (idnum, details, likes, pictures, uploader) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement preparedStatement = conn.prepareStatement(insertSql);
                preparedStatement.setString(1, idnum);
                preparedStatement.setString(2, details);
                preparedStatement.setInt(3, likes);
                preparedStatement.setBytes(4, pictures);
                preparedStatement.setString(5, uploader);

                value = preparedStatement.executeUpdate();
            } else {
                // 图片名称重复，可以选择抛出异常或者进行其他处理
                System.out.println("图片名称重复，不进行插入操作");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnection();
        }
        return value;
    }

    // 查 user，返回1表示查询到对应的id，返回0表示没有找到，返回-1表示查询出现异常
    public static int queryUser(String id) throws SQLException {
        try {
            if (conn == null || conn.isClosed()) {
                conn = getConnection("001for");
            }

            if (conn != null) { // 检查连接是否成功创建
                String sql = "SELECT * FROM users WHERE id=?";
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1, id);

                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    // 如果有匹配的行，返回1
                    return 1;
                } else {
                    // 如果没有匹配的行，返回0
                    return 0;
                }
            } else {
                // 如果连接为null，返回-1表示查询出现异常
                return -1;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return -1; // 返回-1表示查询出现异常
        } finally {
            closeConnection();
        }
    }

    // 查picturestable，返回所有图片、图片主题和作者信息以及每个idnum对应出现的无CAST(likes AS SIGNED) = -1限制下查询的条数
    public static ResultSet queryAllPictures() throws SQLException {
        try {
            if (conn == null || conn.isClosed()) {
                conn = getConnection("001for");
            }

            // 查询每个idnum对应的未应用CAST(likes AS SIGNED) = -1限制下的条数
            String likesCountSql = "SELECT idnum, COUNT(*) AS likes_count FROM picturestable WHERE CAST(likes AS SIGNED) <> -1 GROUP BY idnum";

            // 查询所有图片、图片主题和作者信息
            String picturesSql = "SELECT idnum, pictures, uploader FROM picturestable WHERE CAST(likes AS SIGNED) = -1";

            // 合并两个查询结果
            String sql = String.format("SELECT p.idnum, p.pictures, p.uploader, l.likes_count FROM (%s) AS p LEFT JOIN (%s) AS l ON p.idnum = l.idnum", picturesSql, likesCountSql);

            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            return preparedStatement.executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    // 删 picturestable
    public static int deletePicture(String idnum) {
        int result = 0; // 默认返回0，表示删除失败

        try {
            if (conn == null || conn.isClosed()) {
                conn = getConnection("001for");
            }
            String sql = "DELETE FROM picturestable WHERE idnum=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, idnum);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                result = 1; // 删除成功时返回1
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            try {
                closeConnection();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        return result;
    }

    // 根据id查询头像并返回头像
    public static byte[] queryTouxiangById(String id) throws SQLException {
        try {
            if (conn == null || conn.isClosed()) {
                conn = getConnection("001for");
            }

            String sql = "SELECT touxiang FROM users WHERE id=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // 如果有匹配的行，获取头像数据并返回
                return resultSet.getBytes("touxiang");
            } else {
                // 如果没有匹配的行，返回null表示未找到头像
                return null;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            closeConnection();
        }
    }

    //验证此账号密码是否正确
    public static void verifyUser(String id, String password, Handler handler) {
        try {
            Message message = new Message();
            conn = getConnection("001for");


            if (conn != null) {
                String sql = "SELECT * FROM users WHERE id=? AND password=?";
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1, id);
                preparedStatement.setString(2, password);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    // 用户验证成功
                    Bundle bundle = new Bundle();
                    bundle.putString("account", id);
                    bundle.putString("password", password);
                    message.obj = bundle;
                    message.what = 1;
                } else {
                    // 用户验证失败
                    message.what = 2;
                }
            } else {
                message.what = 2;
            }

            handler.sendMessage(message);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            try {
                closeConnection();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // 查询图片细节
    public static ResultSet queryPictureDetails(String idnum) throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = getConnection("001for");
        }

        String sql = "SELECT details, likes, pictures, uploader FROM picturestable WHERE idnum=?";
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        preparedStatement.setString(1, idnum);

        return preparedStatement.executeQuery();
    }

    public static int querylikes(String idnum,String id) {

        int result = 0;
        try {
            if (conn == null || conn.isClosed()) {
                conn = getConnection("001for");
            }
            String checkSql = "SELECT idnum, likes FROM picturestable WHERE idnum = ? AND likes = ?";
            PreparedStatement checkStatement = conn.prepareStatement(checkSql);
            checkStatement.setString(1, idnum);
            checkStatement.setString(2, id);
            ResultSet resultSet = checkStatement.executeQuery();
            if (resultSet.next()) {
                // 已经存在相同记录，返回2
                result = 1;
            } else {
                result = -1;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return result;
    }
    public static int likeOrUnlikePicture(String idnum, String id, int action,String details,byte[] pictures,String uploader ) {
        int result = 0;
        try {
            if (conn == null || conn.isClosed()) {
                conn = getConnection("001for");
            }

            if (action == 1) {

                // 先检查是否已存在相同记录
                String checkSql = "SELECT idnum, likes FROM picturestable WHERE idnum = ? AND likes = ?";
                PreparedStatement checkStatement = conn.prepareStatement(checkSql);
                checkStatement.setString(1, idnum);
                checkStatement.setString(2, id);

                ResultSet resultSet = checkStatement.executeQuery();

                if (resultSet.next()) {
                    // 已经存在相同记录，返回2
                    result = 2;
                }else {
                String insertSql = "INSERT INTO picturestable (idnum, details,likes, pictures,uploader) VALUES (?,?,?,?,?)";
                PreparedStatement preparedStatement = conn.prepareStatement(insertSql);

                preparedStatement.setString(1, idnum);
                preparedStatement.setString(2, details);
                preparedStatement.setString(3, id);
                preparedStatement.setBytes(4, pictures);
                preparedStatement.setString(5, uploader);

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    result = 1; // 插入成功
                }}
            } else if (action == -1) {
                String deleteSql = "DELETE FROM picturestable WHERE idnum = ? AND likes = ?";
                    PreparedStatement deleteStatement = conn.prepareStatement(deleteSql);
                    deleteStatement.setString(1, idnum);
                    deleteStatement.setString(2, id);

                    int rowsDeleted = deleteStatement.executeUpdate();
                    if (rowsDeleted > 0) {
                        result = -1; // 取消点赞成功
                    }

                // 再次检查是否已经点赞
//                String checkSql = "SELECT idnum, id FROM picturestable WHERE idnum = ? AND id = ?";
//                PreparedStatement checkStatement = conn.prepareStatement(checkSql);
//                checkStatement.setString(1, idnum);
//                checkStatement.setString(2, id);
//
//                ResultSet resultSet = checkStatement.executeQuery();

//                if (resultSet.next()) {
//                    // 已经点赞过就删除记录
//                    String deleteSql = "DELETE FROM picturestable WHERE idnum = ? AND id = ?";
//                    PreparedStatement deleteStatement = conn.prepareStatement(deleteSql);
//                    deleteStatement.setString(1, idnum);
//                    deleteStatement.setString(2, id);
//
//                    int rowsDeleted = deleteStatement.executeUpdate();
//                    if (rowsDeleted > 0) {
//                        result = -1; // 取消点赞成功
//                    }
//                } else {
//                    result = 0;
//                }
            } else {
                throw new IllegalArgumentException("传入参数有误");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            try {
                closeConnection();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        return result;
    }

}
