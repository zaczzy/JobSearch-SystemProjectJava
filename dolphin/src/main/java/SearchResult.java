public class SearchResult {
    private String title;
    private String url;
    private String exerpt;

    public SearchResult(String title, String url, String exerpt) {
        this.title = title;
        this.url = url;
        this.exerpt = exerpt;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getExerpt() {
        return exerpt;
    }

    public void setExerpt(String exerpt) {
        this.exerpt = exerpt;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
