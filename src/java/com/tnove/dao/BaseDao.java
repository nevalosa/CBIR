package com.tnove.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

public class BaseDao  {
	public BaseDao(){
	//	System.out.println("��ʼ���ɹ�");
	}
	
    public void add(Object obj) throws Exception{
        Session session = null;
        try {
            session = HibernateSessionFactory.getSession();
            session.save(obj);
            session.beginTransaction().commit();
            if(session!=null){
                  session.close();
            }
        }catch (RuntimeException e) {
            session.beginTransaction().rollback();
            if(session!=null){
                  session.close();
            }
            System.out.println(e.getMessage());
            throw e;
        }
    }
    
    
    public void delete(Object obj) throws Exception{
        Session session = null;
        try {
            //ȡ��session����
            session =HibernateSessionFactory.getSession();
            //ɾ��ʵ��
            session.delete(obj);
            //�ύ����
            session.beginTransaction().commit();
            if(session!=null){
                  session.close();
            }
        } catch (Exception e) {
            session.beginTransaction().rollback();//����ع�
            if(session!=null){
                  session.close();
            }
            throw e;
        }
    }

    
    public void update(Object obj) throws Exception{
        Session session=null;
        try {
            //ȡ��session����
            session=HibernateSessionFactory.getSession();
            //ɾ��ʵ��
            session.update(obj);
            //�ύ����
            session.beginTransaction().commit();
            if(session!=null){
                  session.close();
            }
        } catch (Exception e) {
            session.beginTransaction().rollback();//����ع�
            if(session!=null){
              session.close();
            }
            throw e;
        }
    }
 
    public void saveOrUpdate(Object obj) throws Exception{
        Session session=null;
        try {
            //ȡ��session����
            session=HibernateSessionFactory.getSession();
            //ɾ��ʵ��
            session.saveOrUpdate(obj);
            //�ύ����
            session.beginTransaction().commit();
            if(session!=null){
                  session.close();
            }
        } catch (Exception e) {
            session.beginTransaction().rollback();//����ع�
            if(session!=null){
              session.close();
            }
            throw e;
        }
    }
    
    public List<?> findByHQL(String hql) throws Exception{
        try {
            Query queryObject =HibernateSessionFactory.getSession().createQuery(hql);
            return queryObject.list();
        } catch (Exception e) {
            throw e;
        }
    }
    
    public List<?> findByHQL(String hql, int index, int amount) throws Exception{
        try {
            Query queryObject =HibernateSessionFactory.getSession().createQuery(hql);
            queryObject.setFirstResult(index);
            queryObject.setMaxResults(amount);
            return queryObject.list();
        } catch (Exception e) {
            throw e;
        }
    }
 /*  
    public Integer getCount(String tableName) throws Exception{
        try {
            Query queryObject =HibernateSessionFactory.getSession().createQuery("select count(id) from "+tableName);
            Integer amount = (Integer)queryObject.uniqueResult();
            return amount;
        } catch (Exception e) {
            throw e;
        }
    }
 */   
    public Object findById(String cls,Serializable key)
        throws Exception
    { 
    	String hql ="from "+cls+" where id = "+ key;
    	try {
            Query queryObject =HibernateSessionFactory.getSession().createQuery(hql);
            return queryObject.uniqueResult();
        } catch (Exception e) {
            throw e;
        }
    }

    public void clear() {
    	Session session = null;
            //ȡ��session����
            session =HibernateSessionFactory.getSession();
            //����ڴ�
            session.flush();
            //�ύ����
            session.clear();
           
	}
}
