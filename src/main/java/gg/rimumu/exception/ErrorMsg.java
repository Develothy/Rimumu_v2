package gg.rimumu.exception;

public enum ErrorMsg {

    ServerException(50000, "서버와 연결이 올바르지 않습니다. 잠시 후 다시 시도 해주세요."),
    EncryptException(50001, " 암.복호화에 실패하였습니다."),

    SummonerNotFound(40410, "님은 존재하지 않는 소환사입니다."),
    MatchNotFound(40411, " match가 존재하지 않습니다."),
    NotFound(40400, " 이(가) 존재하지 않습니다."),
    Invalidation(40000, " 체크 중 에러가 발생했습니다."),

    MemberNotFoundException(40420, "가입되지 않은 회원입니다."),
    MemberAlreadyRegistered(40021, " 이미 가입된 회원입니다."),
    MemberValidation(40022, "이메일 또는 비밀번호를 확인해주세요.");




    private int code;
    private String msg;
    ErrorMsg(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
