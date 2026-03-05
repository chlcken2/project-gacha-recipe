package com.dailytable.dailytable.domain.comment;

public class CommentService {
}

// 해당 레시피에 이미 작성했을 경우 (DB) 조회 -> 결과값읋 utils 함수로 보냄 /= isWrite
//utils (isWrite, user.createdAt, recipe.user_id, userid)

// utils 함수 부분
// 가입일 3일 이내면 false
// 레피시 작성자 id = 본인 id 같으면 fals
//
//　true면 populariy_count 증가 / false 면 패스