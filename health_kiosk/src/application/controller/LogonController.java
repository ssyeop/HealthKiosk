package application.controller;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import application.dao.ManagerDAO;
import application.dao.ManagerDAOImpl;
import application.dto.Manager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class LogonController implements Initializable{

    @FXML
    private TextField id, passwd, chkPasswd, name, email, phoneNum;
    @FXML
    private DatePicker birth; // 생년월일
    @FXML
    private ToggleGroup gender; // 성별
    @FXML
    private Button reg; // 가입하기 버튼
    @FXML
    private AnchorPane window; // 가장 최상위

    private ManagerDAO dao = new ManagerDAOImpl();
    private Manager manager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        // 배경에서 마우스 클릭되거나 엔터버튼이 눌러져서 다른 걸로 넘어갈 때 border를 지우기
        

        // id를 Focus로 시작
        Platform.runLater(()->{
        	id.requestFocus();
        });

        // Enter Key가 눌려지면 다음 창으로 Focus 
        id.setOnKeyPressed(e1->{
        	if(e1.getCode() == KeyCode.ENTER) passwd.requestFocus();
        });
        
        passwd.setOnKeyPressed(e1->{
        	if(e1.getCode() == KeyCode.ENTER) chkPasswd.requestFocus();
        });
        chkPasswd.setOnKeyPressed(e1->{
        	if(e1.getCode() == KeyCode.ENTER) name.requestFocus();
        });
        name.setOnKeyPressed(e1->{
        	if(e1.getCode() == KeyCode.ENTER) email.requestFocus();
        });
        email.setOnKeyPressed(e1->{
        	if(e1.getCode() == KeyCode.ENTER) phoneNum.requestFocus();
        });
        // 마지막 phoneNum 에서 Enter 키 누를 시 가입하기 강제 시작
        phoneNum.setOnKeyPressed(e1->{
        	if(e1.getCode() == KeyCode.ENTER) { reg.fire();
        		}
        });

        // 가입 버튼 클릭
        reg.setOnAction(e-> {
            boolean chkFlag = false;
            
            // 성별 선택
            RadioButton value = (RadioButton)gender.selectedToggleProperty().getValue();
            
            // 회원가입 규칙
            char[] char_id= id.getText().trim().toCharArray();
            char[] char_passwd = passwd.getText().trim().toCharArray();
            String string_name = name.getText().trim();

            chkFlag = chk(char_id, id, "아이디 조건 알림", "아이디는 영문(소문자) 또는 숫자로만 이루어질 수 있습니다.", "아이디 입력 필수", "아이디는 공백일 수 없습니다.", "아이디 다시 입력", "아이디는 12자 이내로 작성해주세요."); // id 체크 완료
            
            if (chkFlag) return; // id 문제 발생하면 종료

            chkFlag = chk(char_passwd, passwd, "비밀번호 조건 알림", "비밀번호는 영문(소문자) 또는 숫자로만 이루어질 수 있습니다.", "비밀번호 입력 필수", "비밀번호는 공백일 수 없습니다.", "비밀번호 다시 입력", "비밀번호는 12자 이내로 작성해주세요."); // 비밀번호 체크 완료

            if (chkFlag) return; // 비밀번호 문제 발생하면 종료

            if (!dao.selectMember(id.getText())) {
                warnPage("아이디 중복", "아이디가 이미 존재합니다. 다른 아이디를 입력해주세요.", id);
                return;
            } else if (!passwd.getText().equals(chkPasswd.getText())) {
                warnPage("비밀번호 불일치", "비밀번호가 일치하지 않습니다. 다시 입력해주세요.", chkPasswd);
                return;
            } // 아이디 중복, 비밀번호 불일치 체크 완료

            // 한글이 아닌경우
            if (string_name != null &&!Pattern.matches("[가-힣]*", string_name)) {
                warnPage("이름 잘못입력", "이름은 한글로만 작성해주세요.", name);
                return;
            } else if (string_name.length() > 4 || string_name.equals("")) {
                warnPage("이름 조건 알림", "이름은 공백이거나 4자리를 초과할 수 없습니다.", name);
                return;
            } // 이름 조건 체크 완료

            // email, phoneNum, birthDate가 공백이어도 회원가입 가능하므로 이름조건 까지 체크 완료하면, 회원가입 가능
            
            // ------------------------------------------------
            // 근데 여기서 또 추가로 email은 20자 이내, 휴대전화는 13자 이내로 만들어야 함
            // 또 처음 시작할 때 id 버튼으로 포커스 이동하고 Enter 클릭하면 다른 포커스로 이동하는 것 구현
            // ------------------------------------------------

            String str_id = id.getText().trim();
            String str_pwd = passwd.getText().trim();
            String str_name = string_name;
            LocalDate date_birthDate = birth.getValue();
            String str_gender = value.getText(); // 성별
            String str_email = email.getText();
            String str_phone = phoneNum.getText();

            manager = new Manager(str_id, str_pwd, str_name, str_gender, date_birthDate, str_email, str_phone);
            // UserVO 에 저장

            dao.join(manager); // DB에 유저 정보 넣기

            Alert info = new Alert(AlertType.INFORMATION);
            info.setHeaderText("회원가입 완료");
            info.showAndWait();
            
            Stage stage = (Stage)reg.getScene().getWindow();
            stage.close(); // DB에 유저 넣고 나면, 창 종료
        }); // 가입버튼 클릭

        
        
    } // end initialize
    
    
    // ID 체크, Passwd 체크
    public boolean chk(char[] charArray, TextField textField, String... text) {
        for (char c : charArray) {
                if (!((c>=48 && c<=57) || (c>=97 && c<=122))) {
                    warnPage(text[0], text[1], textField);
                    return true;
                }
            }
        if (textField.getText().trim().equals("")) {
            warnPage(text[2], text[3],textField);
            return true;
        } else if (textField.getText().trim().length()>12) {
            warnPage(text[4], text[5], textField);
            return true;
        } // ID 체크, Passwd 체크
        return false; // ID 문제 없음
    }

    // warn 페이지 (textfield)
    public static void warnPage(String title, String header,TextField... textFields) {
        Alert warn = new Alert(AlertType.WARNING);
        warn.setTitle(title);
        warn.setHeaderText(header);
        warn.show();
        textFields[0].clear();
        textFields[0].requestFocus();
        textFields[0].setStyle("-fx-border-color:red;");
        if (textFields.length >= 2) {
            for (int i=1; i<textFields.length; i++) {
                textFields[i].clear();
            }
        }
    }

    // warn 페이지 (datepickers)
    public static void warnPage(String title, String header,DatePicker... datePickers) {
        Alert warn = new Alert(AlertType.WARNING);
        warn.setTitle(title);
        warn.setHeaderText(header);
        warn.show();
        datePickers[0].setValue(LocalDate.now());
        datePickers[0].requestFocus();
        if (datePickers.length >= 2) {
            for (int i=1; i<datePickers.length; i++) {
                datePickers[i].setValue(null);
            }
        }
    }


}
