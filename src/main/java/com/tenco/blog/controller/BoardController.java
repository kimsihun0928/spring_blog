package com.tenco.blog.controller;

import com.tenco.blog.model.Board;
import com.tenco.blog.repository.BoardNativeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Controller // IoC
@RequiredArgsConstructor
public class BoardController {

    private final BoardNativeRepository boardNativeRepository;


    /**
     * 게시글 작성 화면 요청
     *
     * @return 페이지 반환
     * 주소설계 : http://localhost:80/board/save-form
     */
    @GetMapping("/board/save-form")
    public String saveForm() {
        return "board/save-form";
    }

    /**
     * 게시글 작성 기능 요청
     *
     * @return 페이지 반환
     * 주소설계 : http://localhost:80/board/save-form
     */
    @PostMapping("/board/save")
    public String saveProc(
            @RequestParam("username") String username,
            @RequestParam("title") String title,
            @RequestParam("content") String content) {

        log.info("username : " + username);
        log.info("title : " + title);
        log.info("content : " + content);

        // insert + 트랜잭션 처리
        boardNativeRepository.save(title, content, username);
        // redirect <-- 다시 URL 요청해!
        // return "redirect:/";
        return "redirect:/";
    }

    /**
     *
     * 게시글 목록 화면 요청
     * 주소설계 : http://localhost:80/
     */
    @GetMapping({"/", "index"})
    public String list(Model model) {
        //
        List<Board> boardList = boardNativeRepository.findAll();
        model.addAttribute("boardList", boardList);
        System.out.println(boardList.toString());
        for (int i = 0; i < boardList.size(); i++) {
            System.out.println(boardList.get(i).getTitle());
            System.out.println("-------------------");
        }
        return "board/list";
    }

}
