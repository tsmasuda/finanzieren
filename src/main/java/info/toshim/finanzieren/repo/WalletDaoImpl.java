package info.toshim.finanzieren.repo;

import java.util.List;

import info.toshim.finanzieren.domain.Wallet;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class WalletDaoImpl implements WalletDao
{
	@Autowired
	private EntityManager em;

	public void save(Wallet wallet)
	{
		em.persist(wallet);
		return;
	}

	public Wallet findById(int id)
	{
		return em.find(Wallet.class, id);
	}

	public List<Wallet> findAll()
	{
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Wallet> criteria = cb.createQuery(Wallet.class);
		Root<Wallet> wallet = criteria.from(Wallet.class);
		criteria.select(wallet);
		return em.createQuery(criteria).getResultList();
	}
}