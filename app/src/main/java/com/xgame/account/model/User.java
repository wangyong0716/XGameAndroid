
package com.xgame.account.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.xgame.app.XgameApplication;
import com.xgame.common.Constants;
import com.xgame.util.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wuyanzhi on 2018/1/25.
 */

public class User implements Parcelable, Data {
    public static final long DEFAULT_USER_ID = -1L;

    public static final int GENDER_MALE = 1;
    public static final int GENDER_FEMALE = 2;

    //TODO
    @SerializedName("userid")
    private long userid = DEFAULT_USER_ID;
    @SerializedName("phone")
    private String phone = "";
    @SerializedName("nickname")
    private String nickname = "";
    @SerializedName("sex")
    private int sex = -1;// 1:male 2:female
    @SerializedName("headimgurl")
    private String headimgurl = "";
    @SerializedName("birthday")
    private String birthday = "";
    @SerializedName("constellation")
    private String constellation = "";
    @SerializedName("age")
    private int age = -1;
    @SerializedName("province")
    private String province = "";
    @SerializedName("city")
    private String city = "";
    @SerializedName("country")
    private String country = "";

    /**
     * 三方的openid
     */
    @SerializedName("qqOpenid")
    private String qqOpenid = "";
    @SerializedName("wxOpenid")
    private String wxOpenid = "";

    @SerializedName("qqName")
    private String qqNickname = "";
    @SerializedName("wxName")
    private String wxNickname = "";

    // not used
    // **********************//
    private String miOpenid = "";
    private String weiboOpenid = "";
    private String weiboNickname = "";
    private String miNickname = "";

    private String usertext = "";
    private String createtime = "";
    private String password = "";
    private String district = "";
    private String background = "";// 背景图片
    // **********************//

    public String getReadableNickName() {
        String ret = nickname;

        if (TextUtils.isEmpty(nickname)) {
            if (!TextUtils.isEmpty(qqOpenid)) {
                ret = qqNickname;
            } else if (!TextUtils.isEmpty(weiboOpenid)) {
                ret = weiboNickname;
            } else if (!TextUtils.isEmpty(wxOpenid)) {
                ret = wxNickname;
            } else if (!TextUtils.isEmpty(miOpenid)) {
                ret = miNickname;
            }
        }

        if (TextUtils.isEmpty(ret)) {
            ret = "用户ID:" + userid;
        }

        return ret;
    }

    public long getUserid() {
        return userid;
    }

    public String getCreatetime() {
        return createtime;
    }

    public String getPhone() {
        return phone;
    }

    public String getPassword() {
        return password;
    }

    public String getNickname() {
        return nickname;
    }

    public int getSex() {
        return sex;
    }

    public String getHeadimgurl() {
        return headimgurl;
    }

    public String getUsertext() {
        return usertext;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getProvince() {
        return province;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }

    public String getBackground() {
        return background;
    }

    public String getQqOpenid() {
        return qqOpenid;
    }

    public String getWeiboOpenid() {
        return weiboOpenid;
    }

    public String getWxOpenid() {
        return wxOpenid;
    }

    public String getMiOpenid() {
        return miOpenid;
    }

    public String getQqNickname() {
        return qqNickname;
    }

    public String getWeiboNickname() {
        return weiboNickname;
    }

    public String getWxNickname() {
        return wxNickname;
    }

    public String getMiNickname() {
        return miNickname;
    }

    public void setUserid(long userid) {
        this.userid = userid;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public void setHeadimgurl(String headimgurl) {
        this.headimgurl = headimgurl;
    }

    public void setUsertext(String usertext) {
        this.usertext = usertext;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getConstellation() {
        if (TextUtils.isEmpty(constellation) && !TextUtils.isEmpty(birthday)) {
            try {
                return StringUtil.getConstellation(birthday);
            } catch (Exception e) {

            }
        }
        return constellation;
    }

    public void setConstellation(String constellation) {
        this.constellation = constellation;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public void setQqOpenid(String qqOpenid) {
        this.qqOpenid = qqOpenid;
    }

    public void setWeiboOpenid(String weiboOpenid) {
        this.weiboOpenid = weiboOpenid;
    }

    public void setWxOpenid(String wxOpenid) {
        this.wxOpenid = wxOpenid;
    }

    public void setMiOpenid(String miOpenid) {
        this.miOpenid = miOpenid;
    }

    public void setQqNickname(String qqNickname) {
        this.qqNickname = qqNickname;
    }

    public void setWeiboNickname(String weiboNickname) {
        this.weiboNickname = weiboNickname;
    }

    public void setWxNickname(String wxNickname) {
        this.wxNickname = wxNickname;
    }

    public void setMiNickname(String miNickname) {
        this.miNickname = miNickname;
    }

    public int getAge() {
        if (age == -1 && !TextUtils.isEmpty(birthday)) {
            try {
                return Integer.valueOf(StringUtil.getAge(XgameApplication.getApplication(), birthday));
            } catch (Exception e) {

            }
        }
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public JSONObject toJSONObject() {
        try {
            return new JSONObject(new Gson().toJson(this));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.userid);
        dest.writeString(this.createtime);
        dest.writeString(this.phone);
        dest.writeString(this.password);
        dest.writeString(this.nickname);
        dest.writeInt(this.sex);
        dest.writeInt(this.age);
        dest.writeString(this.headimgurl);
        dest.writeString(this.usertext);
        dest.writeString(this.birthday);
        dest.writeString(this.province);
        dest.writeString(this.city);
        dest.writeString(this.district);
        dest.writeString(this.background);
        dest.writeString(this.qqOpenid);
        dest.writeString(this.weiboOpenid);
        dest.writeString(this.wxOpenid);
        dest.writeString(this.miOpenid);
        dest.writeString(this.qqNickname);
        dest.writeString(this.weiboNickname);
        dest.writeString(this.wxNickname);
        dest.writeString(this.miNickname);
        dest.writeString(this.constellation);
    }

    protected User(Parcel in) {
        this.userid = in.readLong();
        this.createtime = in.readString();
        this.phone = in.readString();
        this.password = in.readString();
        this.nickname = in.readString();
        this.sex = in.readInt();
        this.age = in.readInt();
        this.headimgurl = in.readString();
        this.usertext = in.readString();
        this.birthday = in.readString();
        this.province = in.readString();
        this.city = in.readString();
        this.district = in.readString();
        this.background = in.readString();
        this.qqOpenid = in.readString();
        this.weiboOpenid = in.readString();
        this.wxOpenid = in.readString();
        this.miOpenid = in.readString();
        this.qqNickname = in.readString();
        this.weiboNickname = in.readString();
        this.wxNickname = in.readString();
        this.miNickname = in.readString();
        this.constellation = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }
        final User user = (User) o;
        if (userid != user.userid) {
            return false;
        }
        if (sex != user.sex) {
            return false;
        }
        if (age != user.age) {
            return false;
        }
        if (phone != null ? !phone.equals(user.phone) : user.phone != null) {
            return false;
        }
        if (nickname != null ? !nickname.equals(user.nickname) : user.nickname != null) {
            return false;
        }
        if (headimgurl != null ? !headimgurl.equals(user.headimgurl) : user.headimgurl != null) {
            return false;
        }
        if (birthday != null ? !birthday.equals(user.birthday) : user.birthday != null) {
            return false;
        }
        if (constellation != null ? !constellation.equals(user.constellation)
                : user.constellation != null) {
            return false;
        }
        if (province != null ? !province.equals(user.province) : user.province != null) {
            return false;
        }
        if (city != null ? !city.equals(user.city) : user.city != null) {
            return false;
        }
        if (country != null ? !country.equals(user.country) : user.country != null) {
            return false;
        }
        if (qqOpenid != null ? !qqOpenid.equals(user.qqOpenid) : user.qqOpenid != null) {
            return false;
        }
        if (wxOpenid != null ? !wxOpenid.equals(user.wxOpenid) : user.wxOpenid != null) {
            return false;
        }
        if (qqNickname != null ? !qqNickname.equals(user.qqNickname) : user.qqNickname != null) {
            return false;
        }
        if (wxNickname != null ? !wxNickname.equals(user.wxNickname) : user.wxNickname != null) {
            return false;
        }
        if (miOpenid != null ? !miOpenid.equals(user.miOpenid) : user.miOpenid != null) {
            return false;
        }
        if (weiboOpenid != null ? !weiboOpenid.equals(user.weiboOpenid)
                : user.weiboOpenid != null) {
            return false;
        }
        if (weiboNickname != null ? !weiboNickname.equals(user.weiboNickname)
                : user.weiboNickname != null) {
            return false;
        }
        if (miNickname != null ? !miNickname.equals(user.miNickname) : user.miNickname != null) {
            return false;
        }
        if (usertext != null ? !usertext.equals(user.usertext) : user.usertext != null) {
            return false;
        }
        if (createtime != null ? !createtime.equals(user.createtime) : user.createtime != null) {
            return false;
        }
        if (password != null ? !password.equals(user.password) : user.password != null) {
            return false;
        }
        if (district != null ? !district.equals(user.district) : user.district != null) {
            return false;
        }
        return background != null ? background.equals(user.background) : user.background == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (userid ^ (userid >>> 32));
        result = 31 * result + (phone != null ? phone.hashCode() : 0);
        result = 31 * result + (nickname != null ? nickname.hashCode() : 0);
        result = 31 * result + sex;
        result = 31 * result + (headimgurl != null ? headimgurl.hashCode() : 0);
        result = 31 * result + (birthday != null ? birthday.hashCode() : 0);
        result = 31 * result + (constellation != null ? constellation.hashCode() : 0);
        result = 31 * result + age;
        result = 31 * result + (province != null ? province.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        result = 31 * result + (qqOpenid != null ? qqOpenid.hashCode() : 0);
        result = 31 * result + (wxOpenid != null ? wxOpenid.hashCode() : 0);
        result = 31 * result + (qqNickname != null ? qqNickname.hashCode() : 0);
        result = 31 * result + (wxNickname != null ? wxNickname.hashCode() : 0);
        result = 31 * result + (miOpenid != null ? miOpenid.hashCode() : 0);
        result = 31 * result + (weiboOpenid != null ? weiboOpenid.hashCode() : 0);
        result = 31 * result + (weiboNickname != null ? weiboNickname.hashCode() : 0);
        result = 31 * result + (miNickname != null ? miNickname.hashCode() : 0);
        result = 31 * result + (usertext != null ? usertext.hashCode() : 0);
        result = 31 * result + (createtime != null ? createtime.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (district != null ? district.hashCode() : 0);
        result = 31 * result + (background != null ? background.hashCode() : 0);
        return result;
    }

    public void copy(User user) {
        if (userid != user.getUserid()) {
            userid = user.getUserid();
        }
        if (sex != user.sex) {
            sex = user.sex;
        }
        if (age != user.age) {
            age = user.age;
        }
        if (shouldUpdate(phone, user.phone)) {
            phone = user.phone;
        }
        if (shouldUpdate(nickname, user.nickname)) {
            nickname = user.nickname;
        }
        if (shouldUpdate(headimgurl, user.headimgurl) && user.headimgurl.startsWith("http")) {
            headimgurl = user.headimgurl;
        }
        if (shouldUpdate(birthday, user.birthday)) {
            birthday = user.birthday;
        }
        if (shouldUpdate(constellation, user.constellation)) {
            constellation = user.constellation;
        }
        if (shouldUpdate(province, user.province)) {
            province = user.province;
        }
        if (shouldUpdate(city, user.city)) {
            city = user.city;
        }
        if (shouldUpdate(country, user.country)) {
            country = user.country;
        }
        if (shouldUpdate(qqOpenid, user.qqOpenid)) {
            qqOpenid = user.qqOpenid;
        }
        if (shouldUpdate(wxOpenid, user.wxOpenid)) {
            wxOpenid = user.wxOpenid;
        }
        if (shouldUpdate(qqNickname, user.qqNickname)) {
            qqNickname = user.qqNickname;
        }
        if (shouldUpdate(wxNickname, user.wxNickname)) {
            wxNickname = user.wxNickname;
        }
    }

    private boolean shouldUpdate(String oldValue, String newValue) {
        if (!TextUtils.equals(oldValue, newValue) && !TextUtils.isEmpty(newValue)) {
            return true;
        }
        return false;
    }
}
