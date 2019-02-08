package com.ayalus.exoplayer2example.model;

public class AccessTokenResponseModel {

    /**
     * access_token : eyJraWQiOiI0NTA2NyIsImFsZyI6IkhTMjU2IiwidHlwIjoiSldUIn0.eyJ2ZXIiOiIxLjAiLCJkZXZpY2VJZCI6IjVjMDdiZjE5MDJjZDNjMDAwMTYyYmVmNSIsInRlbmFudElkIjoibmFncmEiLCJleHAiOjE1NDkwODM0NTcsImFjY291bnRJZCI6IjViZWQwNjM2Yjc4MDc1MDAwMTY1MGFjYSIsImp0aSI6IjU0MWY5OWZlLTI1NjktNGVhYi05MzVkLTE1YjNiNWMwZmMxMyIsInVzZXJJZCI6IkpBQ0siLCJ0eXAiOiJEZXZBdXRoTiJ9.1ZM4XbX6yBUgC1Uva0mzcYoa1Wan6VTroiyzl4_Ndqo
     * client_id : 5c07bf1902cd3c000162bef5
     * accountId : 5bed0636b780750001650aca
     * expires_in : 86400
     * token_type : bearer
     */

    private String access_token;
    private String client_id;
    private String accountId;
    private int expires_in;
    private String token_type;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public int getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(int expires_in) {
        this.expires_in = expires_in;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }
}
