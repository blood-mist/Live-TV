package androidtv.livetv.stb.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import javax.annotation.Generated;

import com.google.gson.annotations.SerializedName;

import static androidtv.livetv.stb.utils.LinkConfig.LOGIN_TABLE;

@Generated("com.robohorse.robopojogenerator")
@Entity(tableName = LOGIN_TABLE)
public class Login {
    @Ignore
    @SerializedName("parent")
    private int parent;

    @Ignore
    @SerializedName("role")
    private String role;

    @Ignore
    @SerializedName("activation_code")
    private Object activationCode;

    @ColumnInfo(name = "session")
    @SerializedName("session")
    private String session;

    @Ignore
    @SerializedName("created_at")
    private String createdAt;

    @Ignore
    @SerializedName("package_id")
    private int packageId;

    @Ignore
    @SerializedName("mac_id")
    private int macId;

    @Ignore
    @SerializedName("lname")
    private String lname;

    @ColumnInfo(name = "community_id")
    @SerializedName("community_id")
    private int communityId;

    @Ignore
    @SerializedName("signup_date")
    private String signupDate;

    @Ignore
    @SerializedName("updated_at")
    private String updatedAt;

    @Ignore
    @SerializedName("card_last_four")
    private Object cardLastFour;

    @Ignore
    @SerializedName("subscription_status")
    private int subscriptionStatus;

    @Ignore
    @SerializedName("card_brand")
    private Object cardBrand;

    @Ignore
    @SerializedName("app_info")
    private String appInfo;

    @PrimaryKey
    @ColumnInfo(name = "user_id")
    @SerializedName("id")
    private int id;

    @ColumnInfo(name = "email")
    @SerializedName("email")
    private String email;

    @Ignore
    @SerializedName("stripe_id")
    private Object stripeId;

    @Ignore
    @SerializedName("fname")
    private String fname;

    @ColumnInfo(name = "is_active")
    @SerializedName("is_active")
    private int isActive;

    @Ignore
    @SerializedName("expiry_date")
    private String expiryDate;

    @Ignore
    @SerializedName("payment_status")
    private int paymentStatus;

    @Ignore
    @SerializedName("firebase_token")
    private Object firebaseToken;

    @ColumnInfo(name = "token")
    @SerializedName("token")
    private String token;

    @Ignore
    @SerializedName("phone")
    private String phone;

    @Ignore
    @SerializedName("updated_by")
    private Object updatedBy;

    @Ignore
    @SerializedName("subscription_period")
    private String subscriptionPeriod;

    @Ignore
    @SerializedName("signup_ip")
    private Object signupIp;

    @Ignore
    @SerializedName("subscription_type")
    private Object subscriptionType;

    @Ignore
    @SerializedName("currency_id")
    private String currencyId;

    @Ignore
    @SerializedName("status")
    private int status;

    public void setParent(int parent) {
        this.parent = parent;
    }

    public int getParent() {
        return parent;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setActivationCode(Object activationCode) {
        this.activationCode = activationCode;
    }

    public Object getActivationCode() {
        return activationCode;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getSession() {
        return session;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setPackageId(int packageId) {
        this.packageId = packageId;
    }

    public int getPackageId() {
        return packageId;
    }

    public void setMacId(int macId) {
        this.macId = macId;
    }

    public int getMacId() {
        return macId;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getLname() {
        return lname;
    }

    public void setCommunityId(int communityId) {
        this.communityId = communityId;
    }

    public int getCommunityId() {
        return communityId;
    }

    public void setSignupDate(String signupDate) {
        this.signupDate = signupDate;
    }

    public String getSignupDate() {
        return signupDate;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setCardLastFour(Object cardLastFour) {
        this.cardLastFour = cardLastFour;
    }

    public Object getCardLastFour() {
        return cardLastFour;
    }

    public void setSubscriptionStatus(int subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public int getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setCardBrand(Object cardBrand) {
        this.cardBrand = cardBrand;
    }

    public Object getCardBrand() {
        return cardBrand;
    }

    public void setAppInfo(String appInfo) {
        this.appInfo = appInfo;
    }

    public String getAppInfo() {
        return appInfo;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setStripeId(Object stripeId) {
        this.stripeId = stripeId;
    }

    public Object getStripeId() {
        return stripeId;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getFname() {
        return fname;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    public int getIsActive() {
        return isActive;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setPaymentStatus(int paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public int getPaymentStatus() {
        return paymentStatus;
    }

    public void setFirebaseToken(Object firebaseToken) {
        this.firebaseToken = firebaseToken;
    }

    public Object getFirebaseToken() {
        return firebaseToken;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setUpdatedBy(Object updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Object getUpdatedBy() {
        return updatedBy;
    }

    public void setSubscriptionPeriod(String subscriptionPeriod) {
        this.subscriptionPeriod = subscriptionPeriod;
    }

    public String getSubscriptionPeriod() {
        return subscriptionPeriod;
    }

    public void setSignupIp(Object signupIp) {
        this.signupIp = signupIp;
    }

    public Object getSignupIp() {
        return signupIp;
    }

    public void setSubscriptionType(Object subscriptionType) {
        this.subscriptionType = subscriptionType;
    }

    public Object getSubscriptionType() {
        return subscriptionType;
    }

    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return
                "Login{" +
                        "parent = '" + parent + '\'' +
                        ",role = '" + role + '\'' +
                        ",activation_code = '" + activationCode + '\'' +
                        ",session = '" + session + '\'' +
                        ",created_at = '" + createdAt + '\'' +
                        ",package_id = '" + packageId + '\'' +
                        ",mac_id = '" + macId + '\'' +
                        ",lname = '" + lname + '\'' +
                        ",community_id = '" + communityId + '\'' +
                        ",signup_date = '" + signupDate + '\'' +
                        ",updated_at = '" + updatedAt + '\'' +
                        ",card_last_four = '" + cardLastFour + '\'' +
                        ",subscription_status = '" + subscriptionStatus + '\'' +
                        ",card_brand = '" + cardBrand + '\'' +
                        ",app_info = '" + appInfo + '\'' +
                        ",id = '" + id + '\'' +
                        ",email = '" + email + '\'' +
                        ",stripe_id = '" + stripeId + '\'' +
                        ",fname = '" + fname + '\'' +
                        ",is_active = '" + isActive + '\'' +
                        ",expiry_date = '" + expiryDate + '\'' +
                        ",payment_status = '" + paymentStatus + '\'' +
                        ",firebase_token = '" + firebaseToken + '\'' +
                        ",token = '" + token + '\'' +
                        ",phone = '" + phone + '\'' +
                        ",updated_by = '" + updatedBy + '\'' +
                        ",subscription_period = '" + subscriptionPeriod + '\'' +
                        ",signup_ip = '" + signupIp + '\'' +
                        ",subscription_type = '" + subscriptionType + '\'' +
                        ",currency_id = '" + currencyId + '\'' +
                        ",status = '" + status + '\'' +
                        "}";
    }
}