package in.tech_camp.protospace_c.factory;

import in.tech_camp.protospace_c.form.CommentForm;

public class CommentFormFactory {
  public static CommentForm createCommentForm() {
    CommentForm commentForm = new CommentForm();

    commentForm.setText("TestComment");

    return commentForm;
  }
}
