package io.sim.auto;

import de.tudresden.sumo.cmd.Vehicle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;
import io.sim.Reconciliation;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Auto extends Thread {

    // Atributos do veículo
    private String idAuto;
    private SumoColor colorAuto;
    private String driverID;
    private SumoTraciConnection sumo;

    private boolean on_off;
    private long acquisitionRate;
    private int fuelType; // 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
    private double fuelPrice; // preço em litros
    private int personCapacity; // o número total de pessoas que podem andar neste veículo
    private int personNumber; // o número total de pessoas que estão andando neste veículo

    private ArrayList<DrivingData> drivingReport;

    private final double totalFuelTank = 10;
    private double currentTank;
    private boolean isRunning;

    private double currentOdometer;
    private double lastOdometer;

    private ArrayList<Double> timeRD;
    private ArrayList<Double> restrictionRD;
    private double routeTime;
    private double startTime;
    private double startAuxTime;
    private double currentSpeed;
    private double speed;
    private double totalDistance;
    private double lastCallDistance = 0;
    private double currentTime;

    private static final String NOME_ARQUIVO = "arquivoExcel.xlsx";
    private static final String NOME_PLANILHA = "MinhaPlanilha";

    public Auto(String _idAuto, SumoColor _colorAuto, SumoTraciConnection _sumo, int _fuelType) throws Exception {
        // Inicializa os atributos do veículo
        this.idAuto = _idAuto;
        this.colorAuto = _colorAuto;
        this.sumo = _sumo;
        this.acquisitionRate = 50;
        this.on_off = false;
        this.currentTank = totalFuelTank;
        this.fuelType = (_fuelType >= 1 && _fuelType <= 4) ? _fuelType : 4;
        this.drivingReport = new ArrayList<>();
        this.currentOdometer = 0;
        this.lastOdometer = 0;
        initializeRD();
    }

    private void initializeRD() {
        // Inicializa o tempo de início
        this.startTime = System.currentTimeMillis();
        // Inicializa o tempo auxiliar
        this.startAuxTime = startTime;
        // Define a distância total como 2500
        this.totalDistance = 2500;
        // Define o tempo total da rota como 60000 milissegundos (60 segundos)
        this.routeTime = 60000;
    
        // Inicializa listas para restrições e tempos de RD (Reconciliation Data)
        this.restrictionRD = new ArrayList<>();
        this.timeRD = new ArrayList<>();
    
        // Adiciona valores iniciais às listas
        timeRD.add(routeTime);
        restrictionRD.add((double) 1);
        for (int i = 0; i < 10; i++) {
            // Divide o tempo total em 10 partes iguais e adiciona à lista de tempos
            timeRD.add((double) (routeTime / 10));
            // Adiciona restrições iniciais à lista de restrições
            restrictionRD.add((double) -1);
        }
    
        // Imprime os valores iniciais da lista de tempos
        for (int i = 0; i < 10; i++) {
            System.out.print(timeRD.get(i) + "\t");
        }
    
        // Calcula a velocidade média com base na distância total e tempo total
        this.speed = totalDistance / (routeTime / 1000);

        writeExcel(speed, 0);
    }
    
    private void RD(double distanceSinceLastCall, double lastCallTime, double speed) {
    
        // Calcula a distância que deveria ser percorrida com base no tempo decorrido e na velocidade
        // double shouldDistance = lastCallTime * speed / 1000;
        // Calcula o erro na distância percorrida
        // double errorDistance = shouldDistance - distanceSinceLastCall;
    
        // Calcula a velocidade média com base na distância e no tempo desde a última chamada
        double averageSpeed = distanceSinceLastCall / (lastCallTime / 1000);
    
        // Calcula o tempo de atraso com base no segundo tempo na lista de tempos de RD
        double lagTime = this.timeRD.get(1) - lastCallTime;
    
        // Calcula o novo tempo para o próximo trecho
        double newTime = this.timeRD.get(0) - this.timeRD.get(1);
    
        // Atualiza a lista de tempos de RD removendo o primeiro elemento e atualizando o segundo
        this.timeRD.set(0, newTime);
        this.timeRD.remove(1);
        this.restrictionRD.remove(1);
    
        // Imprime informações sobre a chamada do método RD
        System.out.println("Tempo após chamada: " + lastCallTime);
        System.out.println("Distância percorrida real: " + distanceSinceLastCall);
        System.out.println("Velocidade média: " + averageSpeed);
        System.out.println("Tempo de atraso: " + lagTime/1000);
        System.out.println("Real tempo: " + lastCallTime/1000);
    
        // Atualiza o segundo tempo na lista de tempos de RD com base no lagTime
        newTime = this.timeRD.get(1) + lagTime;
        timeRD.set(1, newTime);
    
        // Cria arrays para entrada na classe Reconciliation e realiza a reconciliação
        double[] y = new double[timeRD.size()];
        for (int i = 0; i < timeRD.size(); i++) {
            y[i] = timeRD.get(i);
        }
        double[] v = new double[timeRD.size()];
        for (int i = 0; i < timeRD.size(); i++) {
            v[i] = 0.5;
        }
        double[] A = new double[timeRD.size()];
        for (int i = 0; i < timeRD.size(); i++) {
            A[i] = restrictionRD.get(i);
        }
        Reconciliation rec = new Reconciliation(y, v, A);
    
        // Obtém o resultado da reconciliação e atualiza a lista de tempos de RD
        double doub[] = rec.getReconciledFlow();
        for (int i = 0; i < timeRD.size(); i++) {
            timeRD.set(i, (double) doub[i]);
        }
    
        // Imprime o novo tempo para o próximo trecho
        System.out.println("Novo tempo para próximo trecho: " + timeRD.get(1) / 1000);
    
        // Atualiza a velocidade com base no novo tempo
        this.speed = 250 / timeRD.get(1) * 1000;

        writeExcel(averageSpeed, lastCallTime);
    }

    public void writeExcel(double averageSpeed, double lastTime) {
        try {
            // Cria um objeto File para representar o arquivo Excel
            File arquivo = new File(NOME_ARQUIVO);
    
            // Inicializa uma instância de Workbook para manipular o arquivo Excel
            Workbook workbook;
    
            // Verifica se o arquivo já existe e contém dados
            if (arquivo.exists() && arquivo.length() > 0) {
                // Se o arquivo existe e não está vazio, abre o workbook existente
                try (FileInputStream fis = new FileInputStream(arquivo)) {
                    workbook = WorkbookFactory.create(fis);
                }
            } else {
                // Se o arquivo não existe ou está vazio, cria um novo workbook
                workbook = new XSSFWorkbook();
            }
    
            // Obtém a planilha (Sheet) do workbook ou cria uma nova se não existir
            Sheet sheet = workbook.getSheet(NOME_PLANILHA);
            if (sheet == null) {
                sheet = workbook.createSheet(NOME_PLANILHA);
            }
    
            // Encontra a primeira linha vazia na planilha
            int rowNum = findFirstEmptyRow(sheet);
    
            // Obtém a linha (Row) existente ou cria uma nova
            Row row = sheet.getRow(rowNum);
            if (row == null) {
                row = sheet.createRow(rowNum);
            }
    
            // Inicializa o índice da coluna
            int colNum = 0;
    
            // Itera sobre os valores em timeRD e os adiciona às células na linha
            for (Double time : timeRD) {
                Cell cell = row.createCell(colNum++);
                cell.setCellValue(time / 1000);
            }
    
            // Adiciona a média de velocidade (averageSpeed) à célula na coluna atual
            Cell cellAverageSpeed = row.createCell(colNum++);
            cellAverageSpeed.setCellValue(averageSpeed);
    
            // Adiciona o último tempo (lastTime) à célula na próxima coluna
            Cell cellTime = row.createCell(colNum++);
            cellTime.setCellValue(lastTime / 1000);
    
            // Salva o workbook de volta no arquivo Excel
            try (FileOutputStream fileOut = new FileOutputStream(NOME_ARQUIVO)) {
                workbook.write(fileOut);
                System.out.println("Arquivo Excel criado/atualizado com sucesso!");
            }
    
        } catch (IOException e) {
            // Captura e imprime mensagens de erro em caso de falha na manipulação do Excel
            System.out.println("Erro ao criar/atualizar o arquivo Excel!");
            e.printStackTrace();
        }
    }
    

    public void finalizaExcel() {
        try {
            File arquivo = new File(NOME_ARQUIVO);
            Workbook workbook;

            if (arquivo.exists() && arquivo.length() > 0) {
                // Se o arquivo existe e não está vazio, abra o workbook
                try (FileInputStream fis = new FileInputStream(arquivo)) {
                    workbook = WorkbookFactory.create(fis);
                }
            } else {
                // Se o arquivo não existe ou está vazio, crie um novo workbook
                workbook = new XSSFWorkbook();
            }

            // Verifique se a planilha existe no workbook
            Sheet sheet = workbook.getSheet(NOME_PLANILHA);
            if (sheet == null) {
                // Se a planilha não existe, crie-a
                sheet = workbook.createSheet(NOME_PLANILHA);
            }

            // Encontre a primeira linha vazia
            int rowNum = findFirstEmptyRow(sheet);

            // Crie a linha se não houver uma linha vazia
            Row row = sheet.getRow(rowNum);
            if (row == null) {
                // Se a linha não existe, crie-a
                row = sheet.createRow(rowNum);
            }
            int colNum = 0;
            Cell totalTime = row.createCell(colNum);
            totalTime.setCellValue(this.currentTime/1000);

            // Salve o workbook no arquivo
            try (FileOutputStream fileOut = new FileOutputStream(NOME_ARQUIVO)) {
                workbook.write(fileOut);
                System.out.println("Arquivo Excel criado/atualizado com sucesso!");
            }

        } catch (IOException e) {
            System.out.println("Erro ao criar/atualizar o arquivo Excel!");
            e.printStackTrace();
        }
    }

    private int findFirstEmptyRow(Sheet sheet) {
        int rowNum = 0;
        while (rowNum <= sheet.getLastRowNum() && sheet.getRow(rowNum) != null) {
            Row row = sheet.getRow(rowNum);
            if (isRowEmpty(row)) {
                return rowNum;
            }
            rowNum++;
        }
        return rowNum;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }

        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }


    

	@Override
    public void run() {
        this.on_off = true;
        while (this.on_off) {
            try {
                Auto.sleep(this.acquisitionRate);
                this.atualizaSensores();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void atualizaSensores() {
        
			try {
				// Verifique se a conexão com o SUMO está aberta
				if (!this.getSumo().isClosed()) {

                    this.currentTime = System.currentTimeMillis() - startTime;

                    // Verificar a distância percorrida desde a última chamada do método
                    double currentDistance = (double) sumo.do_job_get(Vehicle.getDistance(this.idAuto));
                    double distanceSinceLastCall = currentDistance - lastCallDistance;

                    // Se a distância desde a última chamada for maior ou igual a 500 metros, chama o método específico
                    if (distanceSinceLastCall >= 250 && currentDistance < 2500) {
                        RD(distanceSinceLastCall, System.currentTimeMillis() - startAuxTime, this.currentSpeed);
                        lastCallDistance = currentDistance;  // Atualiza a última distância registrada
                        this.startAuxTime = System.currentTimeMillis();
                    }

                    if(distanceSinceLastCall >= 250 && (currentDistance > 2490 && currentDistance < 2540)){
                        finalizaExcel();
                        lastCallDistance = currentDistance;  // Atualiza a última distância registrada
                        this.startAuxTime = System.currentTimeMillis();
                    }

					this.currentSpeed = (currentTank > 3) ? this.speed : 0;

					SumoPosition2D sumoPosition2D = (SumoPosition2D) sumo.do_job_get(Vehicle.getPosition(this.idAuto));
					// Cria um relatório de condução com dados atualizados
					DrivingData _report;
					_report = new DrivingData(
				        this.idAuto, this.driverID, System.currentTimeMillis(), sumoPosition2D.x, sumoPosition2D.y,
				        (String) this.sumo.do_job_get(Vehicle.getRoadID(this.idAuto)),
				        (String) this.sumo.do_job_get(Vehicle.getRouteID(this.idAuto)),
				        (double) sumo.do_job_get(Vehicle.getSpeed(this.idAuto)),
				        (double) sumo.do_job_get(Vehicle.getDistance(this.idAuto)),
				        (double) sumo.do_job_get(Vehicle.getFuelConsumption(this.idAuto)),
				        1,
				        this.fuelType, this.fuelPrice,
				        (double) sumo.do_job_get(Vehicle.getCO2Emission(this.idAuto)),
				        (double) sumo.do_job_get(Vehicle.getHCEmission(this.idAuto)),
				        this.personCapacity,
				        this.personNumber);

						this.drivingReport.add(_report);

					sumo.do_job_set(Vehicle.setSpeedMode(this.idAuto, 0));
					sumo.do_job_set(Vehicle.setSpeed(this.idAuto, currentSpeed));

					currentOdometer = _report.getOdometer();
					double percorredDistance = currentOdometer - lastOdometer;

					consumeFuel(percorredDistance);

					String info = "AutoID: " + this.getIdAuto() + " -> " +
								"odometer = " + String.format("%.2f", currentOdometer) + " -> " +
								"Tank = " + String.format("%.2f", currentTank) + " -> " + 
                                "Velocidade = " + currentSpeed + " -> " +
                                "Tempo = " + currentTime/1000 +
								"\n*********************************************************************************************";

					System.out.println(info);

					lastOdometer = currentOdometer;
					} else {
						System.out.println("SUMO is closed...");
					}
			} catch (Exception e) {
				e.printStackTrace();
			}

            
    }

    private void consumeFuel(double percorredDistance) {
        // Calcula o consumo de combustível com base na distância percorrida
        currentTank -= percorredDistance / 100;
    }

    // Métodos getters
    public long getAcquisitionRate() {
        return this.acquisitionRate;
    }

    public String getIdAuto() {
        return this.idAuto;
    }

    public SumoTraciConnection getSumo() {
        return this.sumo;
    }

    public int getFuelType() {
        return this.fuelType;
    }

    public SumoColor getColorAuto() {
        return this.colorAuto;
    }

    public boolean isOn() {
        return on_off;
    }

    public void setOn_off(boolean on_off) {
        this.on_off = on_off;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isLowFuel(){
        return (currentTank < 3);
    }

    public void refuelAuto(){
        currentTank = totalFuelTank;
    }

    public double getQuantityToFuel(){
        return totalFuelTank - currentTank;
    }
}
