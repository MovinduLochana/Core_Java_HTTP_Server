public enum MediaType {

    JSON("application/json"),
    TEXT("text/plain"),
    HTML("text/html");

    public final String contentType;

    MediaType(String contentType) {
       this.contentType = contentType;
    }

}
