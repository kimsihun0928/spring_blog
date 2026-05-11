package com.tenco.blog.reply;

import com.tenco.blog.user.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller // IoC
@RequiredArgsConstructor
public class ReplyController {

    private final ReplyService replyService;

    // 댓글 등록 기능 요청
    @PostMapping("/reply/save")
    public String saveProc(ReplyRequest.SaveDTO saveDTO, HttpSession session) {
        // 1. 인증검사(로그인 여부 확인) --> LoginInterceptor처리
        User sessionUser = (User) session.getAttribute("sessionUser");
        // 2. 유효성 검사 (comment 값 확인)
        saveDTO.validate();

        replyService.댓글작성(saveDTO, sessionUser.getId());


        return "redirect:/board/" + saveDTO.getBoardId();
    }

    // 댓글 삭제 기능 요청
    @PostMapping("/reply/{id}/delete")
    public String deleteProc(@PathVariable(name = "id") Integer replyId, HttpSession session,
                             @RequestParam(name = "boardId") Integer boardId) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        replyService.댓글삭제(replyId, sessionUser.getId());

        return "redirect:/board/" + boardId;
    }

}
