package com.tenco.blog.board;

import com.tenco.blog._core.util.Define;
import com.tenco.blog.purchase.PurchaseService;
import com.tenco.blog.reply.ReplyResponse;
import com.tenco.blog.reply.ReplyService;
import com.tenco.blog.user.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller // IoC
@RequiredArgsConstructor // DI
public class BoardController {

    private final BoardService boardService;
    // 댓글 목록 조회시 필요
    private final ReplyService replyService;
    private final PurchaseService purchaseService;

    // /board/{{board.id}}/purchase
    @PostMapping("/board/{id}/purchase")
    public String purchase(@PathVariable(name = "id") Integer boardId, HttpSession session) {
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);

        // ✅ 1. 포인트가 차감된 최신 유저 객체를 서비스로부터 반환받습니다.
        User updatedUser = purchaseService.구매하기(sessionUser.getId(), boardId);

        // ✅ 2. 기존 세션을 최신 유저 정보로 덮어씌웁니다. (세션 동기화)
        session.setAttribute(Define.SESSION_USER, updatedUser);

        // 💡 추가 팁: 리다이렉트 주소에 슬래시(/)가 빠져있었다면 같이 수정해 주세요.
        return "redirect:/board/" + boardId;
    }

    /**
     * 게시글 작성 화면 요청
     *
     * @return 페이지 반환
     * 주소설계 : http://localhost:8080/board/save-form
     */
    @GetMapping("/board/save-form")
    public String saveForm(HttpSession httpSession) {
        return "board/save-form";
    }

    /**
     * 게시글 작성 기능 요청
     *
     * @return 페이지 반환
     * 주소설계 : http://localhost:8080/board/save-form
     */
    @PostMapping("/board/save")
    public String saveProc(BoardRequest.SaveDTO saveDTO, HttpSession session) {
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        saveDTO.validate();
        boardService.게시글작성(saveDTO, sessionUser);
        return "redirect:/";
    }


    /**
     * 게시글 목록 화면 요청
     * 주소설계 : http://localhost:8080/
     */
    // 페이징 처리 주소설계 : http://localhost:8080/?page=1&size=2
    // 페이징 처리 주소설계 : http://localhost:8080/ <--- defaultValue 로 동작
    // @RequestParam(name= "page") 필수 값 처리
    @GetMapping({"/board/list", "/"})
    public String list(Model model,
                       @RequestParam(name = "page", defaultValue = "1") Integer page,
                       @RequestParam(name = "size", defaultValue = "5") Integer size,
                       @RequestParam(name = "keyword", required = false) String keyword) {


        BoardResponse.PageDTO boardPage = boardService.게시글목록(page, size, keyword);
        model.addAttribute("boardPage", boardPage);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        return "board/list";
    }

//    @GetMapping({"/", "index"})
//    public String list(Model model) {
//        List<BoardResponse.ListDTO> boardList = boardService.게시글목록();
//        // OSIV 개념을 false 설정했기 때문에 여기서 LAZY 요청을 하면 터져 버린다.
//        ///boardList.get(0).getUser().getUsername();
//
//        model.addAttribute("boardList", boardList);
//        return "board/list";
//    }

    // 게시글 상세보기 화면 요청
    // http://localhost:8080/board/1
    @GetMapping("/board/{id}")
    public String detailPage(@PathVariable(name = "id") Integer id, Model model, HttpSession session) {

        // 게시글 상세보기는 로그인 하지 않은 사용자도 들어올 수 있음
        User sessionUser = (User) session.getAttribute("sessionUser");
        Integer sessionUserId = sessionUser != null ? sessionUser.getId() : null;

        // 추가 로직 - 유료 게시글이라면 구매 여부 포함해서 상세보기 조회
        BoardResponse.DetailDTO detailDTO = boardService.게시글상세조회(id, sessionUserId);

        // 유료 게시글 경우 - 본인이 작성한 글이라면 보여주어야함
        // 소유자 여부
        boolean isOwner = detailDTO.checkIsOwner(sessionUserId);

        // 머스태치는 AND/OR 같은 논리 연산을 화면에서 못함. 즉, 자바 코드에서 미리 연산해서 내려주어야함
        boolean canRead = !detailDTO.getPremium() || detailDTO.getPurchased() || isOwner;

        List<ReplyResponse.ListDTO> replyList = replyService.댓글목록조회(id, sessionUserId);

        // view 에 데이터 전달- canRead -> true, false
        model.addAttribute("board", detailDTO);
        model.addAttribute("canRead", canRead);
        model.addAttribute("checkIsOwner", detailDTO.checkIsOwner(sessionUserId));
        model.addAttribute("replyList", replyList);

        return "board/detail";
    }


    // 삭제 기능 요청
    @PostMapping("/board/{id}/delete")
    public String deleteProc(@PathVariable(name = "id") Integer id, HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        boardService.게시글삭제(id, sessionUser);
        return "redirect:/";
    }


    // http://localhost:8080/board/1/update-form
    // 게시글 수정 화면 요청
    @GetMapping("/board/{id}/update-form")
    public String updateFormPage(@PathVariable(name = "id") Integer id, Model model, HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        BoardResponse.DetailDTO detailDTO = boardService.게시글상세화면및인가처리(id, sessionUser);
        model.addAttribute("board", detailDTO);
        return "board/update-form";
    }


    @PostMapping("/board/{id}/update")
    public String updateProc(@PathVariable(name = "id") Integer id,
                             BoardRequest.UpdateDTO updateDTO, HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        updateDTO.validate();
        boardService.게시글수정(id, updateDTO, sessionUser);
        return "redirect:/board/" + id;
    }

}
