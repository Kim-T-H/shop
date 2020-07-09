package logic;

import java.io.File;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import dao.ItemDao;
import dao.UserDao;
import dao.SaleDao;
import dao.SaleItemDao;


@Service // @Component +service(Controller �� dao �߰� ����)
public class ShopService {

	@Autowired
	private ItemDao itemDao;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private SaleDao saleDao;
	
	@Autowired
	private SaleItemDao saleItemDao;

	public List<Item> getItemList() {
		return itemDao.list();
	}

	public Item getItem(Integer id) {

		return itemDao.selectOne(id);
	}

	// ���� ���ε�, dao�� ������ ����
	public void itemCreate(Item item, HttpServletRequest request) {
		if (item.getPicture() != null && !item.getPicture().isEmpty()) {

			uploadFileCreate(item.getPicture(), request, "img/");
			item.setPictureUrl(item.getPicture().getOriginalFilename());
		}

		itemDao.insert(item);
	}

	private void uploadFileCreate(MultipartFile picture, HttpServletRequest request, String path) { // picture : ������ ����
																									// ����
		String orgFile = picture.getOriginalFilename();
		String uploadPath = request.getServletContext().getRealPath("/") + path;
		File fpath = new File(uploadPath);
		if (!fpath.exists())
			fpath.mkdirs();
		try {
			// ������ ������ ���� ���Ϸ� ����
			picture.transferTo(new File(uploadPath + orgFile));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void itemUpdate(Item item, HttpServletRequest request) {
		if (item.getPicture() != null && !item.getPicture().isEmpty()) {

			uploadFileCreate(item.getPicture(), request, "img/");
			item.setPictureUrl(item.getPicture().getOriginalFilename());
		}

		itemDao.update(item);

	}

	public void itemdelete(String id) {
		itemDao.delete(id);
		
	}

	public void userInsert(User user) {
		userDao.insert(user);   
		
	}

	public User getUser(String userid) {
		
		return userDao.selectOne(userid);
	}

	
	/*
	 * db�� sale ������ saleitem ���� ����. ����� ������  Sale ��ü�� ����
	 * 1. sale ���̺��� saleid ���� ���� => �ִ밪+1 
	 * 2. sale�� ���� ����. => insert
	 * 3. Cart �����κ��� SaleItem ���� ���� =>insert 
	 * 4. ��� ������  Sale ��ü�� ����
	 */
	
	public Sale checkend(User loginUser, Cart cart) {
		Sale sale= new Sale();
		int maxno=saleDao.getMaxSaleid();  
		sale.setSaleid(++maxno);
		sale.setUser(loginUser);
		sale.setUserid(loginUser.getUserid());
		saleDao.insert(sale);
		List<ItemSet> itemList=cart.getItemSetList(); //Cart ��ǰ����
		int i=0;
		for(ItemSet is : itemList) {
			int seq=++i;
			SaleItem saleItem= new SaleItem(sale.getSaleid(),seq,is);
			sale.getItemList().add(saleItem);
			saleItemDao.insert(saleItem); 
		}
		return sale;
	}

	public List<Sale> salelist(String id) {
		
		return saleDao.list(id); //����� id
	}

	public List<SaleItem> saleItemList(int saleid) {
		
		return saleItemDao.list(saleid); //saleid �ֹ���ȣ
	}

	public void userUpdate(User user) {
		userDao.update(user);
		
	}

	public void userdelete(String userid) {
		userDao.delete(userid);
		
	}

	public List<User> getlistAll() {
		return userDao.getlistAll();
	}

	
	
	
	


}