package gg.rimumu.exception;

public sealed class RimumuException extends Exception {
    public RimumuException(String message) {
        super(message);
    }

    public static final class ServerException extends RimumuException {
        public ServerException() {
            super("서버와 연결이 올바르지 않습니다. 잠시 후 다시 시도 해주세요.");
        }
    }

    public static final class SummonerNotFoundException extends RimumuException {
        public SummonerNotFoundException(String smn) {
            super(smn + "님은 존재하지 않는 소환사입니다.");
        }
    }

    public static final class MatchNotFoundException extends RimumuException {
        public MatchNotFoundException(String match) {
            super(match + " match가 존재하지 않습니다.");
        }
    }

    public static final class MemberAlreadyRegisteredException extends RimumuException {
        public MemberAlreadyRegisteredException(String email) {
            super(email + " 이미 가입된 회원입니다.");
        }
    }

    public static final class MemberValidationException extends RimumuException {
        public MemberValidationException() {
            super("이메일 또는 비밀번호를 확인해주세요.");
        }
    }
}
