package net.cb.cb.library.utils.task;

import retrofit2.Response;

/***
 * @author jyj
 * @date 2016/12/21
 */
public class TaskBean {
    private Integer index=0;
    private Response response;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Response  getResponse() {
        return response;
    }

    public <T>T  getResponseBody() {
        return (T) response.body();
    }
    public void setResponse(Response response) {
        this.response = response;
    }
}
