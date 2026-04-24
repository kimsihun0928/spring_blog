package com.tenco.blog.controller;

import com.tenco.blog.model.Board;
import com.tenco.blog.repository.BoardNativeRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    //  게시글 상세보기 요청 처리
    // http://localhost:80/board/1
    @GetMapping("/board/{id}")
    public String detailPage(@PathVariable(name = "id") Integer id, Model model) {
        // 유효성 검사, 인증 검사
        Board board = boardNativeRepository.findById(id);
        model.addAttribute("board", board);

        return "board/detail";
    }

    // /board/{board.id}/delete
    @PostMapping("/board/{id}/delete")
    public String deleteProc(@PathVariable(name = "id") Integer id) {
        boardNativeRepository.deleteById(id);

        // PRG 패턴 (Post -> Redirect -> Get) 적용
        return "redirect:/";
    }

    // http://localhost:80/board/1/update-form

    @GetMapping("/board/{id}/update-form")
    public String updateFormPage(@PathVariable(name = "id") Integer id, Model model) {
        // 사용자에게 해당 게시물 내용을 보여줘야한다.

        // 조회 기능 - 게시글 id로
        Board board = boardNativeRepository.findById(id);
        model.addAttribute("board", board);

        return "board/update-form";
    }

    // /board/{id}/update

    @PostMapping("/board/{id}/update")
    public String update(@PathVariable(name = "id") Integer id,
                         @RequestParam(name = "username") String username,
                         @RequestParam(name = "title") String title,
                         @RequestParam(name = "content") String content) {

        log.info("username : " + username);
        log.info("title : " + title);
        log.info("content : " + content);
        log.info("id: " + id);

        boardNativeRepository.updateById(username, title, content, id);
        // 게시글 수정 완료 --> 게시글 목록, 게시글 상세보기 화면
        //리다이렉트는 뷰 리졸브 동작이 아닌 (내부 파일 찾는 것이 아니고)
        // 그냥 새로운 HTTP Get 요청이다
        return "redirect:/board/" + id;
        }
    }
