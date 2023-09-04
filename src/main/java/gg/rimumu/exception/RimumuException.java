package gg.rimumu.exception;

public sealed class RimumuException extends Exception {

    public int code;

    public RimumuException(String message) {
        super(message);
    }


    /**
     * SERVER EXCEPTION
     */

    public static final class ServerException extends RimumuException {
        public ServerException() {
            super(ErrorMsg.ServerException.getMsg());
            this.code = ErrorMsg.ServerException.getCode();
        }
    }

    public static final class EncryptException extends RimumuException {
        public EncryptException(String str) {
            super(str + ErrorMsg.EncryptException.getMsg());
            this.code = ErrorMsg.EncryptException.getCode();
        }
    }


    /**
     * SUMMONER EXCEPTION
     */

    public static final class SummonerNotFoundException extends RimumuException {
        public SummonerNotFoundException(String smn) {
            super(smn + ErrorMsg.SummonerNotFound.getMsg());
            this.code = ErrorMsg.SummonerNotFound.getCode();
        }
    }

    public static final class MatchNotFoundException extends RimumuException {
        public MatchNotFoundException(String match) {
            super(match + ErrorMsg.MatchNotFound.getMsg());
            this.code = ErrorMsg.MatchNotFound.getCode();
        }
    }


    /**
     * MEMBER EXCEPTION
     */

    public static final class MemberNotFoundException extends RimumuException {
        public MemberNotFoundException(String email) {
            super(email + ErrorMsg.MemberNotFoundException.getMsg());
            this.code = ErrorMsg.MemberNotFoundException.getCode();
        }

    }    public static final class MemberAlreadyRegisteredException extends RimumuException {
        public MemberAlreadyRegisteredException(String email) {
            super(email + ErrorMsg.MemberAlreadyRegistered.getMsg());
            this.code = ErrorMsg.MemberAlreadyRegistered.getCode();
        }
    }

    public static final class MemberValidationException extends RimumuException {
        public MemberValidationException() {
            super(ErrorMsg.MemberValidation.getMsg());
            this.code = ErrorMsg.MemberValidation.getCode();
        }
    }
}
