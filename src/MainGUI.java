import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.AbstractMap;
import java.util.TreeMap;

class BigDecimalPair extends AbstractMap.SimpleEntry<BigDecimal,BigDecimal> implements Comparable<BigDecimalPair>
{

    public BigDecimalPair(BigDecimal key, BigDecimal value) {
        super(key, value);
    }
    public BigDecimal getUpper() { return getKey()  ;}
    public BigDecimal getLower() { return getValue();}
    @Override
    public int compareTo(BigDecimalPair o) {
        if(this.getUpper().compareTo(o.getUpper()) == 0)
        {
            if(this.getLower().compareTo(o.getLower()) == 0)
                return 0;
            else if(this.getLower().compareTo(o.getLower()) == -1)
                return -1;
            else return 1;
        }
        else if(this.getUpper().compareTo(o.getUpper()) == -1)
            return -1;
        else return 1;
    }
}

public class MainGUI
{
    public static void fill_freq_dic_msg(String msg,TreeMap<Character,BigDecimal> sym_freq)
    {
        BigDecimal one_over_length = BigDecimal.valueOf(1).divide(BigDecimal.valueOf(msg.length()),500,RoundingMode.HALF_UP);
        for(int i = 0;i < msg.length();i++)
        {
            if(sym_freq.containsKey(msg.charAt(i)))
                sym_freq.put(msg.charAt(i),sym_freq.get(msg.charAt(i)).add(one_over_length));
            else
                sym_freq.put(msg.charAt(i),one_over_length);
        }
    }
    public static void fill_ranges_dic(TreeMap<Character,BigDecimal> sym_freq
            ,TreeMap<Character, BigDecimalPair> symTOrange
            ,TreeMap<BigDecimalPair, Character> rangeTOsym)
    {
        BigDecimal curr_highest_range = BigDecimal.valueOf(0),next_highest_range;
        for(char curr_ch : sym_freq.keySet())
        {
            next_highest_range = curr_highest_range.add(sym_freq.get(curr_ch))  ;
            symTOrange.put(curr_ch,new BigDecimalPair(next_highest_range,curr_highest_range));
            rangeTOsym.put(new BigDecimalPair(next_highest_range,curr_highest_range),curr_ch);
            curr_highest_range = next_highest_range;
        }
    }
    public static BigDecimal compress(String msg,TreeMap<Character,BigDecimalPair> symTOrange)
    {
        BigDecimal lower = BigDecimal.valueOf(0), upper = BigDecimal.valueOf(1), range = BigDecimal.valueOf(1);
        for(char curr_sym: msg.toCharArray())
        {
            upper = lower.add(range.multiply(symTOrange.get(curr_sym).getUpper()));
            lower = lower.add(range.multiply(symTOrange.get(curr_sym).getLower()));
            range = upper.subtract(lower);
        }
        BigDecimal comp_val = lower.add(upper).divide(BigDecimal.valueOf(2),500,RoundingMode.HALF_UP);
        return comp_val;
    }
    public static String decompress(BigDecimal code,int length,TreeMap<BigDecimalPair,Character> rangeTOsym)
    {
        String msg = "";
        BigDecimal range;
        for(int i = 0;i < length;i++)
        {
            for (BigDecimalPair curr_range: rangeTOsym.keySet())
            {
                if(curr_range.getUpper().compareTo(code) >= 0 && curr_range.getLower().compareTo(code) <= 0)
                {
                    msg += rangeTOsym.get(curr_range);
                    range = curr_range.getUpper().subtract(curr_range.getLower());
                    code = code.subtract(curr_range.getLower()).divide(range,500,RoundingMode.HALF_UP);
                    break;
                }
            }
        }
        return msg;
    }

    private JButton dec_btn;
    private JButton cmpr_btn;
    private JTextField freq_txt;
    private JTextField code_txt;
    private JTextField msg_txt;
    private JPanel form;
    private JButton freq_btn;
    private JTextField sym_txt;
    private JButton len_btn;
    private JTextField len_txt;
    int unq_number;
    BigDecimal code;
    TreeMap<Character,BigDecimal> sym_freq;
    TreeMap<Character,BigDecimalPair> symTOrange;
    TreeMap<BigDecimalPair,Character> rangeTOsym;

    public MainGUI() {
        cmpr_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sym_freq  = new TreeMap<>();symTOrange = new TreeMap<>();rangeTOsym = new TreeMap<>();
                fill_freq_dic_msg(msg_txt.getText(),sym_freq);
                fill_ranges_dic(sym_freq,symTOrange,rangeTOsym);
                code = compress(msg_txt.getText(),symTOrange);
                String msg = decompress(code,msg_txt.getText().length(),rangeTOsym);
                try {
                    FileOutputStream file = new FileOutputStream("File Output.txt");
                    file.write((code.stripTrailingZeros().toPlainString() + "\n" + msg).getBytes());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        dec_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    cmpr_btn.setVisible(false);cmpr_btn.setEnabled(false);
                    dec_btn.setVisible(false);dec_btn.setEnabled(false);
                    msg_txt.setVisible(false);msg_txt.setEnabled(false);
                    code_txt.setVisible(false);code_txt.setEnabled(false);
                    code = new BigDecimal(code_txt.getText());
                    sym_freq  = new TreeMap<>();symTOrange = new TreeMap<>();rangeTOsym = new TreeMap<>();
                    len_btn.setText("Unique Symbols Number");len_btn.setEnabled(true);len_btn.setVisible(true);
                    len_txt.setEnabled(true);len_txt.setVisible(true);
            }
        });
        len_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                len_txt.setVisible(false);len_txt.setEnabled(false);
                len_btn.setVisible(false);len_btn.setEnabled(false);
                unq_number = Integer.parseInt(len_txt.getText());
                if(sym_freq.size() == 0) {
                    freq_btn.setEnabled(true);freq_btn.setVisible(true);
                    sym_txt.setEnabled(true);sym_txt.setVisible(true);
                    freq_txt.setEnabled(true);freq_txt.setVisible(true);
                }
                else
                {
                    fill_ranges_dic(sym_freq,symTOrange,rangeTOsym);
                    String msg = decompress(code,unq_number,rangeTOsym);
                    BigDecimal code = compress(msg,symTOrange);
                    try {
                        FileOutputStream file = new FileOutputStream("File Output.txt");
                        file.write((msg + "\n" + code.stripTrailingZeros().toPlainString()).getBytes());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    dec_btn.setEnabled(true);dec_btn.setVisible(true);
                    cmpr_btn.setEnabled(true);cmpr_btn.setVisible(true);
                    code_txt.setEnabled(true);code_txt.setVisible(true);
                    msg_txt.setEnabled(true);msg_txt.setVisible(true);
                }
            }
        });
        freq_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(unq_number - 1 == 0){
                    sym_txt.setVisible(false);sym_txt.setEnabled(false);
                    freq_txt.setVisible(false);freq_txt.setEnabled(false);
                    freq_btn.setVisible(false);freq_btn.setEnabled(false);
                    sym_freq.put(sym_txt.getText().charAt(0),new BigDecimal(freq_txt.getText()));
                    unq_number--;
                    len_btn.setText("Insert Length");len_btn.setEnabled(true);len_btn.setVisible(true);
                    len_txt.setEnabled(true);len_txt.setVisible(true);
                }
                else
                {
                    sym_freq.put(sym_txt.getText().charAt(0),new BigDecimal(freq_txt.getText()));
                    unq_number--;
                }
            }
        });
    }
    public static void main(String[] args)
    {
        JFrame jf = new JFrame("Main GUI");
        jf.setContentPane(new MainGUI().form);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);
    }
}
