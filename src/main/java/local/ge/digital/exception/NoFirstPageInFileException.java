package local.ge.digital.exception;

public class NoFirstPageInFileException extends RuntimeException {
    public NoFirstPageInFileException() {
        super("Could not read the first page from json string");
    }
}
