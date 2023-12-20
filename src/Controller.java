import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.zip.*;


public class Controller {
    public static Pracownik newEmployee(){
        String pesel;
        String name;
        String lastName;
        BigDecimal salary;
        String phoneNumber;
        Pracownik temp = null;

        System.out.println("__________________________________________");

        String position;

        Scanner scan  = new Scanner(System.in);
        do{
            System.out.print("[D]yrektor/[H]andlowiec      :        ");
            position = scan.nextLine();
        }while (!isValidPosition(position));
        System.out.println("__________________________________________");
        System.out.print("Indetyfikator PESEL          :      ");
        pesel = scan.nextLine();
        while(!checkPesel(pesel)){
            System.out.println("Invalid PESEL");
            System.out.print("Indetyfikator PESEL          :      ");
            pesel = scan.nextLine();
          };
          System.out.print("Imię                         :      ");
          name = scan.nextLine();
          System.out.print("Nazwisko                     :      ");
          lastName = scan.nextLine();
          System.out.print("Wynagrodzenie                :      ");
          salary = getBigDecimalFromUser();
          System.out.print("Telefon służbowy numer       :      ");
          phoneNumber = scan.nextLine();
          if(phoneNumber.equals("")){
              phoneNumber = "-brak-";
          }

        if(position.equals("D")){
            BigDecimal serviceAllowance;
            String cardNumber;
            BigDecimal costLimit;
            System.out.print("Dodatek slużbowy             :      ");
            serviceAllowance =getBigDecimalFromUser();
            System.out.print("Karta sluzbowa numer         :      ");
            cardNumber = scan.nextLine();
            if(cardNumber.equals("")){
                cardNumber = "-brak-";
            }
            System.out.print("Limit kosztów/miesiąc(zł)    :      ");
            costLimit = getBigDecimalFromUser();
            temp =new Dyrektor(pesel,name,lastName,salary,phoneNumber,serviceAllowance,cardNumber,costLimit);

        } else if (position.equals("H")) {
            BigDecimal commission;
            BigDecimal commissionLimit;
            System.out.print("Prowizja %                   :      ");
            commission = getBigDecimalFromUser();
            System.out.print("Limit prowizji/miesiąc(zł)   :      ");
            commissionLimit = getBigDecimalFromUser();
            temp = new Handlowiec(pesel,name,lastName,salary,phoneNumber,commission,commissionLimit);
        }

        return temp;

    }
    private static boolean  isValidPosition(String choose){
        return choose.equals("D") || choose.equals("H");
    }
    private static boolean  isValidCopyChoose(String choose){
        return choose.equalsIgnoreCase("Z") || choose.equalsIgnoreCase("O");
    }

    public static void showMenu(DataBase dataBase){
        int choose = 0;
        do{
            try{
                System.out.println("MENU");

        System.out.println("\t 1. Lista pracowników");
        System.out.println("\t 2. Dodaj pracownika");
        System.out.println("\t 3. Usuń pracownika");
        System.out.println("\t 4. Kopia zapasowa");
        System.out.println("\t 0. End program");
        System.out.print("Wybór -> ");
        Scanner scan  = new Scanner(System.in);
         choose = scan.nextInt();
        switch (choose){
            case 1: {
                System.out.println("Lista pracowników");
                dataBase.showAllEmployeers();}break;
            case 2: {
                System.out.println("Dodanie Pracownika: ");

                Pracownik temp = newEmployee();
                if(dataBase.has(temp)){
                    temp.showEmployee();
                    System.out.println("Dodanie Pracownika: ");
                    if(getKey()){
                    dataBase.addEmployee(temp);
                    saveEmployee(temp);
                    System.out.println("New Employee added");
                    }else{
                        System.out.println("New Employee not added");
                    }
                }else{
                    System.out.println("Pracownik o podanym numerze PESEL już istnieje");
                }
            }break;
            case 3:{
                System.out.println("Usuniencie pracownika");
                System.out.print("Wprowadz numer PESEL pracownika do usunięcia :");
                Scanner scanner = new Scanner(System.in);
                String tempPesel = scanner.nextLine();
                Pracownik employee = dataBase.searchEmployee(tempPesel);
                if(employee != null){
                    employee.showEmployee();
                    System.out.println("Usuniencie pracownika");
                    if(getKey()){
                    dataBase.removeEmployee(employee);
                    System.out.println("Employee removed Successfully");
                    }else{
                        System.out.println("Employee removed Aborted");
                    }
                }
            }break;
            case 4: {
                String choose3;

                Scanner scanner  = new Scanner(System.in);
                do{
                    System.out.print("[Z]achowaj/[O]dtwórz       :        ");
                    choose3 = scanner.nextLine();
                }while (!isValidCopyChoose(choose3));
                if(choose3.equalsIgnoreCase("Z")){
                    saveAsyncEmployees(dataBase);

                } else if (choose3.equalsIgnoreCase("O")) {
                   getAsyncEmployees(dataBase);
                }
            }break;
        }
            }catch (Exception e){
                System.out.println("Nieprawidlowy wybór");
                e.printStackTrace();
            }
        }while (choose !=0);
    }
    private static boolean getKey(){
        String choose = "";
        do{
        System.out.println("[Enter] - Podtwierdz \n [Q] Porzuc");
        Scanner scan = new Scanner(System.in);
         choose = scan.nextLine();
        if(choose.equals("")) {
            return true;
        } else if (choose.equals("Q") || choose.equals("q")) {
            return false;
        }
        else{
            System.out.println("Nierozpoznawalny wybor");
        }
        }while(true);
    }
    public static boolean getKeyNext(){
        String choose = "";
        do{
        System.out.println("[Enter] - Next employee \n [Q] Quit");
        Scanner scan = new Scanner(System.in);
         choose = scan.nextLine();
        if(choose.equals("")) {
            return true;
        } else if (choose.equals("Q") || choose.equals("q")) {
            return false;
        }
        else{
            System.out.println("Nierozpoznawalny wybor");
        }
        }while(true);
    }
    private static boolean checkPesel(String pesel){
       if(pesel != null && pesel.length() == 11){
           for (char c:pesel.toCharArray()) {
               if(!Character.isDigit(c)){
                   return false;
               }

           }
           if(checkPeselF(pesel)){
           return true;
           }else{
               return false;
           }
       }
       return false;
    }
    private static boolean checkPeselF(String pesel){
        return ((Integer.parseInt(String.valueOf(pesel.charAt(0)))) + 3 *(Integer.parseInt(String.valueOf(pesel.charAt(1)))) +7*(Integer.parseInt(String.valueOf(pesel.charAt(2)))) +
                9 *(Integer.parseInt(String.valueOf(pesel.charAt(3)))) +(Integer.parseInt(String.valueOf(pesel.charAt(4)))) + 3*(Integer.parseInt(String.valueOf(pesel.charAt(5)))) +
                7 *(Integer.parseInt(String.valueOf(pesel.charAt(6)))) + 9 * (Integer.parseInt(String.valueOf(pesel.charAt(7)))) +(Integer.parseInt(String.valueOf(pesel.charAt(8)))) +
                3*(Integer.parseInt(String.valueOf(pesel.charAt(9)))) + (Integer.parseInt(String.valueOf(pesel.charAt(10))))) %10 == 0;
    }

    private static BigDecimal getBigDecimalFromUser(){
        Scanner scan = new Scanner(System.in);
        BigDecimal result;
        while(true){
            try{
                result = new BigDecimal(scan.nextLine());
                return result;
            }catch (Exception e){
                System.out.println("The value must be a Number");
                System.out.print("                             :      ");
            }
        }
    }

    private static void createCopy(DataBase dataBase){
        try{
            FileOutputStream fileOut = new FileOutputStream("./data/backup.txt");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);

            dataBase.createCopytemp(out);
            out.writeObject(new EndOfFile());
            out.close();
            fileOut.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void getCopy(DataBase dataBase){
        try{
            FileInputStream fileInputStream = new FileInputStream("./data/backup.txt");
            ObjectInputStream in = new ObjectInputStream(fileInputStream);
            Object obj = null;
            dataBase.clear();
            while((obj = in.readObject()) instanceof EndOfFile == false){
                dataBase.addEmployee((Pracownik) obj);
            }
            in.close();
            fileInputStream.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    private static  void zipCompression(String outfile, File dirToZip) throws IOException {
        int iByte = 0;
        FileOutputStream f = new FileOutputStream(outfile);
        CheckedOutputStream ad32o = new CheckedOutputStream(f,new Adler32());
        ZipOutputStream zos = new ZipOutputStream(ad32o);
        BufferedOutputStream out  = new BufferedOutputStream(zos);
        for (File infile: dirToZip.listFiles()
             ) {
                BufferedInputStream in  = new BufferedInputStream(new FileInputStream(infile));
                zos.putNextEntry(new ZipEntry(infile.getName()));
                while ((iByte = in.read()) != -1){
                    out.write(iByte);
                }
                in.close();
                out.flush();
        }
        out.close();

    }

    private static void zipDecompression(String inFile, File outDir) throws IOException {
        int iByte = 0;
        FileInputStream fi = new FileInputStream(inFile);
        CheckedInputStream ad32i = new CheckedInputStream(fi, new Adler32());
        ZipInputStream in  = new ZipInputStream(ad32i);
        BufferedInputStream bis = new BufferedInputStream(in);
        String fileName;
        int i = 0;
        while(in.getNextEntry() != null){
            i++;
            FileWriter  temp = new FileWriter("./data/employees/" + String.valueOf(i) + ".txt");
            while ((iByte = bis.read() )!= -1){
                temp.write(iByte);
                temp.flush();
            }

        }
        bis.close();

    }

    public static void saveEmployee (Pracownik employee){
        try{

            FileOutputStream fileOut = new FileOutputStream("./data/employees/"+employee.getPesel()+".txt");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);

            out.writeObject(employee);

            out.close();
            fileOut.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }


    public static Pracownik getEmployee(String path){
        try{
            FileInputStream fileInputStream = new FileInputStream(path);
            ObjectInputStream in = new ObjectInputStream(fileInputStream);
            Object obj = null;
            obj = in.readObject();


            in.close();
            fileInputStream.close();
            return (Pracownik) obj;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static void saveAsyncEmployees(DataBase dataBase){
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            ArrayList<CompletableFuture<Void>> completableFutures = new ArrayList<>();
            dataBase.getMainDataBase().forEach((pesel, employee) -> {
                completableFutures.add(CompletableFuture.runAsync(new WriteRunnable(employee), executorService));
            });
            for (CompletableFuture<Void> cFut : completableFutures
            ) {
                try {
                    cFut.get();
                } catch (ExecutionException e) {
                    System.out.println("Interrupted exception: " + e);
                } catch (InterruptedException e) {
                    System.out.println("Execution exception: " + e);
                }
            }
            System.out.print("Wpisz Nazwe backup        :        ");
            Scanner scanner  = new Scanner(System.in);
            String backupName = scanner.nextLine();
            File outdir = new File("./data/employees/");
            zipCompression("./backups/"+backupName+".zip", outdir);
//            for (File tempfile: outdir.listFiles()
//                 ) {
//                tempfile.delete();
//            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private static void getAsyncEmployees(DataBase dataBase) throws IOException{
        try {

            ExecutorService executorService = Executors.newFixedThreadPool(10);
            ArrayList<CompletableFuture<Pracownik>> completableFutures = new ArrayList<>();

            File employeesData = new File("./data/employees/");

            for (File employeeFile : employeesData.listFiles()
                 ) {
                completableFutures.add(CompletableFuture.supplyAsync( new SupplierPracownik(employeeFile.getPath()), executorService));
            }

            for (CompletableFuture<Pracownik> cFut : completableFutures
            ) {
                try {
                    cFut.get();
                    dataBase.addEmployee(cFut.get());
                } catch (ExecutionException e) {
                    System.out.println("Interrupted exception: " + e);
                } catch (InterruptedException e) {
                    System.out.println("Execution exception: " + e);
                }
            }
            System.out.println("Odczytano Asynchronicznie");
        }catch (Exception e){
            e.printStackTrace();

        }

    }

}



