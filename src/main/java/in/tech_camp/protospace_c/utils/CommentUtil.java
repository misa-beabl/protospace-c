package in.tech_camp.protospace_c.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.tech_camp.protospace_c.entity.CommentEntity;

public class CommentUtil {
  public static List<CommentEntity> buildCommentTree(List<CommentEntity> allComments) {
    Map<Integer, CommentEntity> map = new HashMap<>();
    List<CommentEntity> roots = new ArrayList<>();

    for (CommentEntity comment : allComments) {
        map.put(comment.getId(), comment);
        if (comment.getReplyComments() == null) {
            comment.setReplyComments(new ArrayList<>());
        }
    }

    for (CommentEntity comment : allComments) {
        CommentEntity parent = comment.getParentComment();
        if (parent == null || parent.getId() == null) {
            roots.add(comment);
        } else {
            CommentEntity parentInMap = map.get(parent.getId());
            if (parentInMap != null) {
                if (parentInMap.getReplyComments() == null) {
                    parentInMap.setReplyComments(new ArrayList<>());
                }
                parentInMap.getReplyComments().add(comment);
            }
        }
    }
    return roots;
}
}
