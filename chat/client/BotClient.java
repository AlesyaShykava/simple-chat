package main.java.chat.client;

import main.java.chat.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class BotClient extends Client {
    public class BotSocketThread extends SocketThread {
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            BotClient.this.sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            String[] nameAndText = message.split(": ");
            if (nameAndText.length == 2) {
                String messageText = nameAndText[1];
                Calendar currentCalendar = Calendar.getInstance();
                Date currentDate = currentCalendar.getTime();
                SimpleDateFormat simpleDateFormat = null;
                if (messageText.equals("дата")) {
                    simpleDateFormat = new SimpleDateFormat("d.MM.YYYY");
                } else if (messageText.equals("день")) {
                    simpleDateFormat = new SimpleDateFormat("d");
                } else if (messageText.equals("месяц")) {
                    simpleDateFormat = new SimpleDateFormat("MMMM");
                } else if (messageText.equals("год")) {
                    simpleDateFormat = new SimpleDateFormat("YYYY");
                } else if (messageText.equals("время")) {
                    simpleDateFormat = new SimpleDateFormat("H:mm:ss");
                } else if (messageText.equals("час")) {
                    simpleDateFormat = new SimpleDateFormat("H");
                } else if (messageText.equals("минуты")) {
                    simpleDateFormat = new SimpleDateFormat("m");
                } else if (messageText.equals("секунды")) {
                    simpleDateFormat = new SimpleDateFormat("s");
                }
                if (simpleDateFormat != null) {
                    BotClient.this.sendTextMessage(String.format("Информация для %s: %s", nameAndText[0], simpleDateFormat.format(currentDate)));
                }
            }
        }
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        return String.format("date_bot_%d", (int) (Math.random() * 100));
    }

    public static void main(String[] args) {
        BotClient bot = new BotClient();
        bot.run();
    }
}
