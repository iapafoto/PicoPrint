///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package picoprint;
//
//import java.io.IOException;
//import java.util.jar.Attributes;
//import org.xml.sax.SAXException;
//import org.xml.sax.XMLReader;
//import org.xml.sax.helpers.DefaultHandler;
//
///**
// *
// * @author durands
// */
//public class SvgParser {
//
//    static class TestSAX2Handler extends DefaultHandler {
//
//        private String tagCourant = "";
//
//        /**
//         * Actions à réaliser lors de la détection d'un nouvel élément.
//         */
//        public void startElement(String nameSpace, String localName,
//                String qName, Attributes attr) throws SAXException {
//            tagCourant = localName;
//            System.out.println("debut tag : " + localName);
//        }
//
//        /**
//         * Actions à réaliser lors de la détection de la fin d'un élément.
//         */
//        public void endElement(String nameSpace, String localName,
//                String qName) throws SAXException {
//            tagCourant = "";
//            System.out.println("Fin tag " + localName);
//        }
//
//        /**
//         * Actions à réaliser au début du document.
//         */
//        public void startDocument() {
//            System.out.println("Debut du document");
//        }
//
//        /**
//         * Actions à réaliser lors de la fin du document XML.
//         */
//        public void endDocument() {
//            System.out.println("Fin du document");
//        }
//
//        /**
//         * Actions à réaliser sur les données
//         */
//        @Override
//        public void characters(char[] caracteres, int debut,
//                int longueur) throws SAXException {
//            String donnees = new String(caracteres, debut, longueur);
//
//            if (!tagCourant.equals("")) {
//                if (!Character.isISOControl(caracteres[debut])) {
//                    System.out.println("   Element " + tagCourant + ",  valeur = *" + donnees + "*");
//                }
//            }
//        }
//
//        public static void main(String[] args) {
//
//            try {
//                Class c = Class.forName("org.apache.xerces.parsers.SAXParser");
//                XMLReader reader = (XMLReader) c.newInstance();
//                TestSAX2Handler handler = new TestSAX2Handler();
//                reader.setContentHandler(handler);
//                reader.parse("test.xml");
//
//                 Document document = null;
//        try {
//            document = saxBuilder.build(f);
//        } catch (JDOMException | IOException e) {
//            e.printStackTrace();
//        }
//
//        if (document == null) {
//            return;
//        }
//        
//        Element rootElement = document.getRootElement();
//
//                
//            } catch (Exception e) {
//                System.out.println(e);
//            }
//
//        }
//
//    }
//}
