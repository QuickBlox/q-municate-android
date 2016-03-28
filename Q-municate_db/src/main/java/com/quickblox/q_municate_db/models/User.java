package com.quickblox.q_municate_db.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

import static com.quickblox.q_municate_db.models.User.Column.AVATAR;
import static com.quickblox.q_municate_db.models.User.Column.EMAIL;
import static com.quickblox.q_municate_db.models.User.Column.FULL_NAME;
import static com.quickblox.q_municate_db.models.User.Column.ID;
import static com.quickblox.q_municate_db.models.User.Column.LAST_LOGIN;
import static com.quickblox.q_municate_db.models.User.Column.LOGIN;
import static com.quickblox.q_municate_db.models.User.Column.PHONE;
import static com.quickblox.q_municate_db.models.User.Column.ROLE;
import static com.quickblox.q_municate_db.models.User.Column.STATUS;
import static com.quickblox.q_municate_db.models.User.Column.TABLE_NAME;

@DatabaseTable(tableName = TABLE_NAME)
public class User implements Serializable {

    @DatabaseField(
            id = true,
            unique = true,
            columnName = ID)
    private int userId;

    @DatabaseField(
            foreign = true,
            foreignAutoRefresh = true,
            canBeNull = true,
            columnName = Social.Column.ID)
    private Social social;

    @DatabaseField(
            columnName = ROLE)
    private Role role;

    @DatabaseField(
            columnName = FULL_NAME)
    private String fullName;

    @DatabaseField(
            columnName = EMAIL)
    private String email;

    @DatabaseField(
            columnName = LOGIN)
    private String login;

    @DatabaseField(
            columnName = PHONE)
    private String phone;

    @DatabaseField(
            columnName = AVATAR)
    private String avatar;

    @DatabaseField(
            columnName = STATUS)
    private String status;

    @DatabaseField(
            columnName = LAST_LOGIN)
    private long lastLogin;

    public User() {
    }

    public User(Integer userId, Social social, Role role, String fullName, String email, String phone,
            String avatar) {
        this.userId = userId;
        this.social = social;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.avatar = avatar;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
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

        return userId == user.getUserId();
    }

    @Override
    public int hashCode() {
        return userId;
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

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    @Override
    public String toString() {
        return "User [userId='" + userId
                + "', fullName='" + fullName + "'"
                + "', email='" + email + "']";
    }

    public enum Role {

        OWNER(0),
        SIMPLE_ROLE(1);

        private int code;

        Role(int code) {
            this.code = code;
        }

        public static Role parseByCode(int code) {
            Role[] valuesArray = Role.values();
            Role result = null;
            for (Role value : valuesArray) {
                if (value.getCode() == code) {
                    result = value;
                    break;
                }
            }
            return result;
        }

        public int getCode() {
            return code;
        }
    }

    public interface Column {

        String TABLE_NAME = "user";
        String ID = "user_id";
        String FULL_NAME = "full_name";
        String EMAIL = "email";
        String LOGIN = "login";
        String PHONE = "phone";
        String AVATAR = "avatar";
        String STATUS = "status";
        String ROLE = "role";
        String LAST_LOGIN = "last_login";
    }
}