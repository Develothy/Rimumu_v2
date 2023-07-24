package gg.rimumu.exception;

public enum ErrorMsg {

    ServerException("서버와 연결이 올바르지 않습니다. 잠시 후 다시 시도 해주세요."),

    SummonerNotFound("님은 존재하지 않는 소환사입니다."),
    MatchNotFound(" match가 존재하지 않습니다."),

    MemberAlreadyRegistered( " 이미 가입된 회원입니다."),
    MemberValidation("이메일 또는 비밀번호를 확인해주세요.");





    private String msg;
    ErrorMsg(String msg) {
        this.msg = msg;
    }
    public String getMsg() {
        return msg;
    }
}
