package lyrth.makanism.api;

public abstract class BotCommand extends Command {

    @Override
    public boolean isGuildOnly() {
        return false;
    }
}
