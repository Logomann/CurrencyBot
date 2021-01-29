import org.json.simple.parser.ParseException;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bot extends TelegramLongPollingBot {


    private final String TOKEN = "BOT_TOKEN";
    private final String BOT_NAME = "BOT_NAME";
    private HashMap<String,Float> ratesAndPairs = new HashMap<>();
    private final HashMap<Integer,String> usersIdAndCurrencies = new HashMap<>();

    @Override
    public void onUpdateReceived(Update update) {
        String message;
        if (update.hasMessage() && update.getMessage().hasText()) {
                message = update.getMessage().getText();
                try {
                    sendMsg(update.getMessage().getChatId().toString(), message, update.getMessage().getFrom().getId());
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

        } else if (update.hasCallbackQuery()) {
            usersIdAndCurrencies.put(update.getCallbackQuery().getFrom().getId(),update.getCallbackQuery().getData());
            try {
                execute(new SendMessage().setChatId(update.getCallbackQuery().
                        getMessage().getChatId()).setText("Введите сумму"));
                } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        }
    }
    private synchronized void sendMsg(String chatId, String s, int userId) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        if (s.equals("/start")) try {
            ratesAndPairs = HttpRequest.getCurrencyData();
            sendMessage.setReplyMarkup(setInlineKeyboard(ratesAndPairs));
            sendMessage.setText("Выберите валюту");
            execute(sendMessage);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        else if(s.matches("[-+]?\\d+") && !usersIdAndCurrencies.isEmpty()){
            sendMessage.setText(calculateAmount(s,userId));
            execute(sendMessage);
        }else {
            sendMessage.setText("Для начала работы или смены валюты введите /start");
            execute(sendMessage);
        }

    }
      private String calculateAmount(String s, int userId){
        float f = ratesAndPairs.get(usersIdAndCurrencies.get(userId));
        float d = f * Float.parseFloat(s);
        String result = String.format("%.2f",d) + " " + usersIdAndCurrencies.get(userId).replaceAll("USD","") +
                " по курсу " + String.format("%.2f",f) + " на " + HttpRequest.getCurrencyDate();

        return   result;
    }

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }
    private InlineKeyboardMarkup setInlineKeyboard(HashMap<String,Float> pairsAndRatesMap){
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttonsInner = new ArrayList<>();
        List<InlineKeyboardButton> buttonsInner1 = new ArrayList<>();
        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        for(Map.Entry<String,Float> pair : pairsAndRatesMap.entrySet()){
            String name = pair.getKey().replaceAll("USD","USD в ");
            if (buttonsInner.size()<2) buttonsInner.add(new InlineKeyboardButton().setText(name).setCallbackData(pair.getKey()));
            else buttonsInner1.add(new InlineKeyboardButton().setText(name).setCallbackData(pair.getKey()));

        }
        buttons.add(buttonsInner);
        buttons.add(buttonsInner1);
        markupKeyboard.setKeyboard(buttons);
        return markupKeyboard;
    }

    @Override
    public String getBotToken() {
        return TOKEN;
    }
}
