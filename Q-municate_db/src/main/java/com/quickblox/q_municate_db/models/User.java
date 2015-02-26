package com.quickblox.q_municate_db.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName = User.TABLE_NAME)
public class User implements Serializable {

    public static final String TABLE_NAME = "user";

    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_FULL_NAME = "full_name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_LOGIN = "login";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_AVATAR = "avatar";
    public static final String COLUMN_STATUS = "status";

    @DatabaseField(id = true, unique = true, columnName = COLUMN_USER_ID)
    private Integer userId;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = false,
            columnName = Role.COLUMN_ROLE_ID)
    private Role role;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = true,
            columnName = Social.COLUMN_SOCIAL_ID)
    private Social social;

    @DatabaseField(columnName = COLUMN_FULL_NAME)
    private String fullName;

    @DatabaseField(columnName = COLUMN_EMAIL)
    private String email;

    @DatabaseField(columnName = COLUMN_LOGIN)
    private String login;

    @DatabaseField(columnName = COLUMN_PHONE)
    private String phone;

    @DatabaseField(columnName = COLUMN_AVATAR)
    private String avatar;

    @DatabaseField(columnName = COLUMN_STATUS)
    private String status;

    public User() {
    }

    public User(Integer userId, Role role, Social social, String fullName, String email, String phone,
            String avatar) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.avatar = avatar;
        this.role = role;
        this.social = social;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof User)) {
            return false;
        }

        User user = (User) object;

        return userId.equals(user.getUserId());
    }

    @Override
    public int hashCode() {
        return userId.hashCode();
    }

    @Override
    public String toString() {
        return fullName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Social getSocial() {
        return social;
    }

    public void setSocial(Social social) {
        this.social = social;
    }
}