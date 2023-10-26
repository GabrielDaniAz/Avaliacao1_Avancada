package io.sim.company.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import io.sim.bank.client.BankDocument;

public class Company implements Runnable {

    private ArrayList<String[]> availableRoutes; // Lista de rotas disponíveis para a empresa
    private final int PORT = 54321; // Porta do servidor da empresa

    private BankDocument bankDocument; // Documento bancário da empresa

    public Company() {
        this.availableRoutes = new ArrayList<>();
        loadRoutesFromXML("data/dadosMap.xml"); // Carrega as rotas do arquivo XML
    }

    private void loadRoutesFromXML(String xmlFilePath) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(xmlFilePath));
            NodeList vehicleList = doc.getElementsByTagName("vehicle");

            for (int i = 0; i < vehicleList.getLength(); i++) {
                Node vehicleNode = vehicleList.item(i);
                if (vehicleNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element vehicleElement = (Element) vehicleNode;
                    Node routeNode = vehicleElement.getElementsByTagName("route").item(0);
                    if (routeNode != null && routeNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element edges = (Element) routeNode;
                        String[] route = new String[] { String.valueOf(i + 1), edges.getAttribute("edges") };
                        availableRoutes.add(route);
                    }
                }
            }
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public synchronized String[] getRandomRoute() {
        if (!availableRoutes.isEmpty()) {
            int randomIndex = (int) (Math.random() * availableRoutes.size());
            return availableRoutes.remove(randomIndex);
        }
        return null;
    }

    @Override
    public void run() {
        try {
            openAccount(); // Abre uma conta bancária para a empresa
            ServerSocket serverSocket = new ServerSocket(PORT); // Cria um servidor de soquete
            System.out.println("Classe Company abriu o servidor na porta " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();

                new Thread(new CompanyHandler(this, clientSocket)).start(); // Inicia um manipulador de empresa em uma nova thread
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openAccount() {
        this.bankDocument = new BankDocument("Company");
        this.bankDocument.openAccount(); // Abre uma conta bancária para a empresa
    }
}
