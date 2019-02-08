package com.ayalus.exoplayer2example.model;

public class ContentTokenResponseModel {

    /**
     * token_type : bearer
     * content_token : eyJraWQiOiI0NTA2NyIsImFsZyI6IkhTMjU2IiwidHlwIjoiSldUIn0.eyJ2ZXIiOiIxLjAiLCJleHAiOjE1NDg5Mzk1ODQsImNvbnRlbnRSaWdodHMiOlt7ImR1cmF0aW9uIjo4NjQwMCwic3RvcmFibGUiOmZhbHNlLCJjb250ZW50SWQiOiJUQk5fU0QtTkciLCJzdGFydCI6IjIwMTktMDEtMzFUMTI6NTQ6NDRaIiwiZW5kIjoiMjAxOS0wMi0wMVQxMjo1NDo0NFoifV0sImp0aSI6ImEyMDAzOGU3LWY5MDctNDZhMC1hYWEyLTQ5MjdiNzdjMDcwOCIsImRldmljZSI6eyJhY2NvdW50SWQiOiI1YmVkMDYzNmI3ODA3NTAwMDE2NTBhY2EifSwidHlwIjoiQ29udGVudEF1dGhaIn0.ZYXUrAAlvlMDmnmJIr_hqei3xJgynD_H5sdMIsyBDtk
     * expires_in : 300
     */

    private String token_type;
    private String content_token;
    private int expires_in;

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public String getContent_token() {
        return content_token;
    }

    public void setContent_token(String content_token) {
        this.content_token = content_token;
    }

    public int getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(int expires_in) {
        this.expires_in = expires_in;
    }
}
