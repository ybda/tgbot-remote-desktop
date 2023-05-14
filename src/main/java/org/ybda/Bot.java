package org.ybda;

import org.ini4j.Ini;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;


public class Bot extends TelegramLongPollingBot {
    String botToken;
    String botUsername;
    long myChatId;

    @Override
    public String getBotUsername() {
        return botUsername;
    }
    @Override
    public String getBotToken() {
        return botToken;
    }
    @Override
    public void onUpdateReceived(Update update) {
        Message msg = update.getMessage();
        if (validateMsg(msg)) {
            try {
                handleMsg(msg);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    public static void run(final File cfgFile) {
        try {
            new TelegramBotsApi(DefaultBotSession.class).registerBot(new Bot(cfgFile));
            System.out.println("Bot started");
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void initBotCredentials(final File cfgFile) {
        try {
            Ini ini = new Ini(cfgFile);
            final String sectionName = "TelegramBot";
            botToken = ini.get(sectionName, "bot_token");
            botUsername = ini.get(sectionName, "bot_username");
            myChatId = Long.parseLong(ini.get(sectionName, "my_chat_id"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    Bot(final File cfgFile) {
        initBotCredentials(cfgFile);
    }

    private boolean validateMsg(Message msg) {
        return msg != null && msg.hasText() && msg.getChatId() == myChatId;
    }

    private void sendMsg(String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(myChatId);
        sendMessage.setText(text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendImg(BufferedImage image) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", os);
            InputStream is = new ByteArrayInputStream(os.toByteArray());
            InputFile inputFile = new InputFile(is, "image.jpg");
            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(myChatId);
            sendPhoto.setPhoto(inputFile);
            execute(sendPhoto);
        } catch (IOException | TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public BufferedImage captureScreen() {
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        try {
            return new Robot().createScreenCapture(screenRect);
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    public void drawCircle(BufferedImage img, int x, int y) {
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.BLUE); // Set the color of the circle to red
        int diameter = 20; // Set the diameter of the circle to 40 pixels
        Stroke stroke = new BasicStroke(6f); // Set the thickness of the circle to 10 pixels
        g2d.setStroke(stroke); // Set the stroke of the Graphics2D object
        g2d.drawOval(x - diameter/2, y - diameter/2, diameter, diameter); // Draw the circle
    }

    private void onMsgHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("* 0 - capture screen").append('\n');
        sb.append("* 12.424 - highlight coords to click").append('\n');
        sb.append("* 13.42= - click by coords").append('\n');
        sb.append("* 13.43% - click by coords, wait 1 second and send capture");
        sendMsg(sb.toString());
    }

    private void onMsgZero() {
        BufferedImage capture = captureScreen();
        sendImg(capture);
    }

    private void handleMsg(Message msg) {
        String text = msg.getText();
        char lastChar = text.charAt(text.length() - 1);

        if (text.equals("help") || text.equals("h") || text.equals("/help")) {
            onMsgHelp();
        } else if (text.equals("0")) {
            onMsgZero();
        } else {
            String[] coordsAsStr = text.split("\\.");
            if (coordsAsStr.length < 2) {
                onMsgHelp();
                return;
            }

            for (int i = 0; i < coordsAsStr.length; i++) {
                coordsAsStr[i] = coordsAsStr[i].trim();
            }
            boolean hasLetter = !Character.isDigit(lastChar);
            char letter = hasLetter ? coordsAsStr[1].charAt(coordsAsStr[1].length() - 1) : ' ';

            String xStr = coordsAsStr[0];
            String yStr = hasLetter ? coordsAsStr[1].substring(0, coordsAsStr[1].length() - 1) : coordsAsStr[1];

            int x = Integer.parseInt(xStr), y = Integer.parseInt(yStr);

            if (!hasLetter) {
                BufferedImage capture = captureScreen();
                drawCircle(capture, x, y);
                sendImg(capture);

            } else if (lastChar == '=') {
                Robotx.leftClick(x, y);

            } else if (lastChar == '%') {
                Robotx.leftClick(x, y);
                Robotx.sleep(1000);
                BufferedImage capture = captureScreen();
                drawCircle(capture, x, y);
                sendImg(capture);
            }
        }
    }
}
