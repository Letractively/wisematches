package wisematches.server.web.controllers.playground.form;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public class MessageForm {
    @NotEmpty(message = "asdasd")
    private String msgText;
    private long msgBoard;
    private long msgOriginal;
    private long msgRecipient;

    public MessageForm() {
    }

    public long getMsgRecipient() {
        return msgRecipient;
    }

    public void setMsgRecipient(long msgRecipient) {
        this.msgRecipient = msgRecipient;
    }

    public String getMsgText() {
        return msgText;
    }

    public void setMsgText(String msgText) {
        this.msgText = msgText;
    }

    public long getMsgOriginal() {
        return msgOriginal;
    }

    public void setMsgOriginal(long msgOriginal) {
        this.msgOriginal = msgOriginal;
    }

    public long getMsgBoard() {
        return msgBoard;
    }

    public void setMsgBoard(long msgBoard) {
        this.msgBoard = msgBoard;
    }
}
