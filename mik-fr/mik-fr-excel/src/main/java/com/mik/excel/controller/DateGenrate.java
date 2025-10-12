package com.mik.excel.controller;

import java.util.ArrayList;
import java.util.List;

public class DateGenrate {

    public static List<Income> gen(){
        List<Income> incomeList = new ArrayList<Income>();

        for(int i=1; i<=30; i++){
            Income income = new Income();
            income.setDate(String.valueOf(i));
            income.setIncome((int) (Math.random() * 100 + 1));
            incomeList.add(income);
        }
        return incomeList;
    }
}
