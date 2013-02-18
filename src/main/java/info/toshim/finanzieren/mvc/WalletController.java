package info.toshim.finanzieren.mvc;

import info.toshim.finanzieren.domain.Balance;
import info.toshim.finanzieren.domain.BalancePk;
import info.toshim.finanzieren.domain.Category;
import info.toshim.finanzieren.domain.Currency;
import info.toshim.finanzieren.domain.Wallet;
import info.toshim.finanzieren.mvc.core.ListOfDates;
import info.toshim.finanzieren.repo.BalanceDao;
import info.toshim.finanzieren.repo.CategoryDao;
import info.toshim.finanzieren.repo.CurrencyDao;
import info.toshim.finanzieren.repo.KindDao;
import info.toshim.finanzieren.repo.WalletDao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "/")
public class WalletController
{
	private static final Logger log = Logger.getLogger(WalletController.class);

	@Autowired
	private WalletDao walletDao;

	@Autowired
	private CategoryDao categoryDao;

	@Autowired
	private CurrencyDao currencyDao;

	@Autowired
	private KindDao kindDao;

	@Autowired
	private BalanceDao balanceDao;

	@InitBinder
	public void initBinder(WebDataBinder binder)
	{
		CustomDateEditor editor = new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"), true);
		binder.registerCustomEditor(Date.class, editor);
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String displayStatus(Model model)
	{
		return "redirect:/list";
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String displayList(Model model)
	{
		List<Wallet> listWallet = walletDao.findAll();
		List<Balance> listBalance = balanceDao.findByUserid("a34256c6bc043f5e081c39cd58fb03f1");
		model.addAttribute("listWallet", listWallet);
		model.addAttribute("listBalance", listBalance);
		return "list";
	}

	@RequestMapping(value = "/refresh", method = RequestMethod.GET)
	public String runRefreshBalance(Model model)
	{
		Double refreshSum = 0.0;
		String userid = "a34256c6bc043f5e081c39cd58fb03f1";
		List<Wallet> listWallet = walletDao.findAll();
		for (int i = 0; i < listWallet.size(); i++)
		{
			if (listWallet.get(i).getKind().getId() % 2 != 0)
			{
				refreshSum -= listWallet.get(i).getAmount();
			} else
			{
				refreshSum += listWallet.get(i).getAmount();
			}
		}
		BalancePk balancePk = new BalancePk(userid, 1);
		Balance balance = balanceDao.findByBalance(balancePk);
		if (balance != null)
		{
			balance.setSum(refreshSum);
			balanceDao.update(balance);
		} else
		{
			balance = new Balance();
			balance.setUserid(userid);
			balance.setCurrencyid(1);
			balance.setCurrency(listWallet.get(1).getCurrency());
			balance.setSum(refreshSum);
			balanceDao.save(balance);
		}
		return "redirect:/list";
	}

	@RequestMapping(value = "/list/{id}/delete", method = RequestMethod.GET)
	public String deleteList(@PathVariable("id") int id)
	{
		walletDao.delete(id);
		return "redirect:/refresh";
	}

	@RequestMapping(value = "/list/{id}/edit", method = RequestMethod.GET)
	public String editList(@PathVariable("id") int id, Model model)
	{
		Wallet wallet = walletDao.findById(id);
		String retPath = "";
		switch (wallet.getKind().getId())
		{
		case 1:
			retPath = "exp";
			break;
		case 2:
			retPath = "inc";
			break;
		case 3:
			retPath = "tro";
			break;
		case 4:
			retPath = "tri";
			break;
		}
		List<Currency> listWlcurrency = currencyDao.findAll();
		List<Category> listWlcategory = categoryDao.findByKindId(wallet.getKind().getId());
		String strDate = new SimpleDateFormat("yyyy-MM-dd").format(wallet.getDate());
		List<String> listWlDate = new ArrayList<String>();
		listWlDate.add(strDate);
		model.addAttribute("regWalletRecord", wallet);
		model.addAttribute("listWlcurrency", listWlcurrency);
		model.addAttribute("listWlcategory", listWlcategory);
		model.addAttribute("listWlDate", listWlDate);
		return retPath;
	}

	@RequestMapping(value = "/list/{id}/edit", method = RequestMethod.POST)
	public String editList(@Valid @ModelAttribute("regWalletRecord") Wallet wallet, BindingResult result, Model model)
	{
		walletDao.update(wallet);
		this.calcBalanceByNewRecord(wallet);
		return "redirect:/list";
	}

	@RequestMapping(value = "/exp", method = RequestMethod.GET)
	public String displayExp(Model model)
	{
		List<Currency> listWlcurrency = currencyDao.findAll();
		List<Category> listWlcategory = categoryDao.findByKindId(1);
		ListOfDates listOfDates = new ListOfDates();
		List<String> listWlDate = listOfDates.getListOfDates(10, ListOfDates.DAY_MODE);
		Wallet wallet = new Wallet();
		model.addAttribute("regWalletRecord", wallet);
		model.addAttribute("listWlcurrency", listWlcurrency);
		model.addAttribute("listWlcategory", listWlcategory);
		model.addAttribute("listWlDate", listWlDate);
		return "exp";
	}

	@RequestMapping(value = "/exp", method = RequestMethod.POST)
	public String registerExp(@Valid @ModelAttribute("regWalletRecord") Wallet wallet, BindingResult result, Model model)
	{
		if (!result.hasErrors())
		{
			walletDao.save(wallet);
			this.calcBalanceByNewRecord(wallet);
			return "redirect:/exp";
		} else
		{
			List<Currency> listWlcurrency = currencyDao.findAll();
			List<Category> listWlcategory = categoryDao.findByKindId(1);
			ListOfDates listOfDates = new ListOfDates();
			List<String> listWlDate = listOfDates.getListOfDates(10, ListOfDates.DAY_MODE);
			model.addAttribute("regWalletRecord", wallet);
			model.addAttribute("listWlcurrency", listWlcurrency);
			model.addAttribute("listWlcategory", listWlcategory);
			model.addAttribute("listWlDate", listWlDate);
			return "exp";
		}
	}

	@RequestMapping(value = "/inc", method = RequestMethod.GET)
	public String displayInc(Model model)
	{
		List<Currency> listWlcurrency = currencyDao.findAll();
		List<Category> listWlcategory = categoryDao.findByKindId(2);
		ListOfDates listOfDates = new ListOfDates();
		List<String> listWlDate = listOfDates.getListOfDates(10, ListOfDates.DAY_MODE);
		Wallet wallet = new Wallet();
		model.addAttribute("regWalletRecord", wallet);
		model.addAttribute("listWlcurrency", listWlcurrency);
		model.addAttribute("listWlcategory", listWlcategory);
		model.addAttribute("listWlDate", listWlDate);
		return "inc";
	}

	@RequestMapping(value = "/inc", method = RequestMethod.POST)
	public String registerInc(@Valid @ModelAttribute("regWalletRecord") Wallet wallet, BindingResult result, Model model)
	{
		if (!result.hasErrors())
		{
			walletDao.save(wallet);
			this.calcBalanceByNewRecord(wallet);
			return "redirect:/inc";
		} else
		{
			List<Currency> listWlcurrency = currencyDao.findAll();
			List<Category> listWlcategory = categoryDao.findByKindId(2);
			ListOfDates listOfDates = new ListOfDates();
			List<String> listWlDate = listOfDates.getListOfDates(10, ListOfDates.DAY_MODE);
			model.addAttribute("regWalletRecord", wallet);
			model.addAttribute("listWlcurrency", listWlcurrency);
			model.addAttribute("listWlcategory", listWlcategory);
			model.addAttribute("listWlDate", listWlDate);
			return "inc";
		}
	}

	@RequestMapping(value = "/tro", method = RequestMethod.GET)
	public String displayTro(Model model)
	{
		List<Currency> listWlcurrency = currencyDao.findAll();
		List<Category> listWlcategory = categoryDao.findByKindId(3);
		ListOfDates listOfDates = new ListOfDates();
		List<String> listWlDate = listOfDates.getListOfDates(10, ListOfDates.DAY_MODE);
		Wallet wallet = new Wallet();
		model.addAttribute("regWalletRecord", wallet);
		model.addAttribute("listWlcurrency", listWlcurrency);
		model.addAttribute("listWlcategory", listWlcategory);
		model.addAttribute("listWlDate", listWlDate);
		return "tro";
	}

	@RequestMapping(value = "/tro", method = RequestMethod.POST)
	public String registerTro(@Valid @ModelAttribute("regWalletRecord") Wallet wallet, BindingResult result, Model model)
	{
		if (!result.hasErrors())
		{
			walletDao.save(wallet);
			this.calcBalanceByNewRecord(wallet);
			return "redirect:/tro";
		} else
		{
			List<Currency> listWlcurrency = currencyDao.findAll();
			List<Category> listWlcategory = categoryDao.findByKindId(3);
			ListOfDates listOfDates = new ListOfDates();
			List<String> listWlDate = listOfDates.getListOfDates(10, ListOfDates.DAY_MODE);
			model.addAttribute("regWalletRecord", wallet);
			model.addAttribute("listWlcurrency", listWlcurrency);
			model.addAttribute("listWlcategory", listWlcategory);
			model.addAttribute("listWlDate", listWlDate);
			return "tro";
		}
	}

	@RequestMapping(value = "/tri", method = RequestMethod.GET)
	public String displayTri(Model model)
	{
		List<Currency> listWlcurrency = currencyDao.findAll();
		List<Category> listWlcategory = categoryDao.findByKindId(4);
		ListOfDates listOfDates = new ListOfDates();
		List<String> listWlDate = listOfDates.getListOfDates(10, ListOfDates.DAY_MODE);
		Wallet wallet = new Wallet();
		model.addAttribute("regWalletRecord", wallet);
		model.addAttribute("listWlcurrency", listWlcurrency);
		model.addAttribute("listWlcategory", listWlcategory);
		model.addAttribute("listWlDate", listWlDate);
		return "tri";
	}

	@RequestMapping(value = "/tri", method = RequestMethod.POST)
	public String registerTri(@Valid @ModelAttribute("regWalletRecord") Wallet wallet, BindingResult result, Model model)
	{
		if (!result.hasErrors())
		{
			walletDao.save(wallet);
			this.calcBalanceByNewRecord(wallet);
			return "redirect:/tri";
		} else
		{
			List<Currency> listWlcurrency = currencyDao.findAll();
			List<Category> listWlcategory = categoryDao.findByKindId(4);
			ListOfDates listOfDates = new ListOfDates();
			List<String> listWlDate = listOfDates.getListOfDates(10, ListOfDates.DAY_MODE);
			model.addAttribute("regWalletRecord", wallet);
			model.addAttribute("listWlcurrency", listWlcurrency);
			model.addAttribute("listWlcategory", listWlcategory);
			model.addAttribute("listWlDate", listWlDate);
			return "tri";
		}
	}

	private void calcBalanceByNewRecord(Wallet wallet)
	{
		BalancePk balancePk = new BalancePk(wallet.getUserid(), wallet.getCurrency().getId());
		Balance balance = balanceDao.findByBalance(balancePk);
		if (balance == null)
		{
			balance = new Balance();
			balance.setUserid(wallet.getUserid());
			balance.setCurrencyid(wallet.getCurrency().getId());
			balance.setCurrency(wallet.getCurrency());
			balanceDao.save(balance);
		}
		Double tmpAmount = 0.0;
		if (wallet.getKind().getId() % 2 != 0)
		{
			tmpAmount = -1 * wallet.getAmount();
		} else
		{
			tmpAmount = wallet.getAmount();
		}
		balance.setSum(tmpAmount + balance.getSum());
		balance.setCurrency(wallet.getCurrency());
		balanceDao.update(balance);
	}
}