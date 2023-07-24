package gg.rimumu.exception;

public sealed class RimumuException extends Exception {

    public RimumuException(String message) {
        super(message);
    }


    /**
     * SERVER EXCEPTION
     */

    public static final class ServerException extends RimumuException {
        public ServerException() {
            super(ErrorMsg.ServerException.getMsg());
        }
    }


    /**
     * SUMMONER EXCEPTION
     */

    public static final class SummonerNotFoundException extends RimumuException {
        public SummonerNotFoundException(String smn) {
            super(smn + ErrorMsg.SummonerNotFound.getMsg());
        }
    }

    public static final class MatchNotFoundException extends RimumuException {
        public MatchNotFoundException(String match) {
            super(match + ErrorMsg.MatchNotFound.getMsg());
        }
    }


    /**
     * MEMBER EXCEPTION
     */

    public static final class MemberAlreadyRegisteredException extends RimumuException {
        public MemberAlreadyRegisteredException(String email) {
            super(email + ErrorMsg.MemberAlreadyRegistered.getMsg());
        }
    }

    public static final class MemberValidationException extends RimumuException {
        public MemberValidationException() {
            super(ErrorMsg.MemberValidation.getMsg());
        }
    }
}
