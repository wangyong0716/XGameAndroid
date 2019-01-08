
package com.xgame.account.api;

import com.google.gson.annotations.SerializedName;
import com.xgame.base.api.DataProtocol;

public class AvatarResult implements DataProtocol {

    /**
     * {"code":200,"data":{"imageUrl":"XGAME_RS_ROOT_URLimage/1517659881982-unknown-679"},"timestamp":1517659886660}
     */
    @SerializedName("imageUrl")
    private String imageUrl;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


}
