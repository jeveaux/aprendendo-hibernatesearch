package com.jeveaux.palestra.hibernatesearch;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;

import com.jeveaux.palestra.hibernatesearch.entity.Evento;

public class IndexerAndSearcher {
	
	EntityManagerFactory entityManagerFactory = 
		Persistence.createEntityManagerFactory("aprendendo-hibernatesearchPU");
	
	private void startIndex() {
		EntityManager em = entityManagerFactory.createEntityManager();
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);
		em.getTransaction().begin();

		List<Evento> eventos = em.createQuery("select evento from Evento as evento").getResultList();
		for (Evento evento : eventos) {
		    fullTextEntityManager.index(evento);
		} 

		em.getTransaction().commit();
		em.close();
	}
	
	/**
	 * 
	 * @param text
	 * @throws ParseException
	 */
	public void search(String text, String[] fields) throws ParseException {
		EntityManager em = entityManagerFactory.createEntityManager();
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);
		em.getTransaction().begin();

		// Cria a query do lucene
		MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, new StandardAnalyzer());
		org.apache.lucene.search.Query query = parser.parse(text);

		javax.persistence.Query persistenceQuery = fullTextEntityManager.createFullTextQuery(query, Evento.class);

		// Executa a pesquisa
		List<Evento> result = persistenceQuery.getResultList();
		
		// Exibe os documentos encontrados na pesquisa  
		System.out.println("Pesquisando por [" + text + "] \n\t" +
				"[" + result.size()+ "] resultados encontrados");
		
		for(Evento evento : result) {
			System.out.println("\t\t" + " -> " + evento.getNome() + " - " + evento.getDescricao());
		} 
		
		em.getTransaction().commit();
		em.close();
	}
	
	/**
	 * 
	 */
	public void preencherBaseDeDados() {
		EntityManager em = entityManagerFactory.createEntityManager();
		
		em.getTransaction().begin();
		
		salvarNovoEvento(em, "Café com Tapioca", "Evento sobre Java do CEJUG em Fortaleza", "www.cejug.org");
		salvarNovoEvento(em, "JustJava", "Evento sobre Java do SouJava em São Paulo", "www.justjava.org");
		salvarNovoEvento(em, "EJES", "Encontro de Java do Espírito Santo organizado pelo ESJUG", "www.esjug.org");
		salvarNovoEvento(em, "Javali", "Java e Software Livre no FISL", "www.javali.org");
		
		em.getTransaction().commit();
		em.close();
	}
	
	private void salvarNovoEvento(EntityManager em, String nome, 
			String descricao, String site) {
		
		Evento evento = new Evento();
		evento.setNome(nome);
		evento.setDescricao(descricao);
		evento.setSite(site);
		
		// Salva o novo evento no banco de dados
		em.persist(evento);
	}

	public static void main(String[] args) {
		IndexerAndSearcher indexerAndSearch = new IndexerAndSearcher();
		
		// Preenche o banco de dados com alguns registros
		indexerAndSearch.preencherBaseDeDados();
		try {
			String[] fields = new String[]{"nome", "descricao"};
			indexerAndSearch.search("Café", fields);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
}
