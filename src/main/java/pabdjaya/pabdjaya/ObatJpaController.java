/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pabdjaya.pabdjaya;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import pabdjaya.pabdjaya.exceptions.IllegalOrphanException;
import pabdjaya.pabdjaya.exceptions.NonexistentEntityException;
import pabdjaya.pabdjaya.exceptions.PreexistingEntityException;

/**
 *
 * @author Rifqi Ichsan A
 */
public class ObatJpaController implements Serializable {

    public ObatJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory(pabdjaya_pabdjaya_jar_0.0.1-SNAPSHOTPU);

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Obat obat) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Transaksi transaksi = obat.getTransaksi();
            if (transaksi != null) {
                transaksi = em.getReference(transaksi.getClass(), transaksi.getKodeTransaksi());
                obat.setTransaksi(transaksi);
            }
            Supplier supplier = obat.getSupplier();
            if (supplier != null) {
                supplier = em.getReference(supplier.getClass(), supplier.getIdPerusahaan());
                obat.setSupplier(supplier);
            }
            em.persist(obat);
            if (transaksi != null) {
                Obat oldObatOfTransaksi = transaksi.getObat();
                if (oldObatOfTransaksi != null) {
                    oldObatOfTransaksi.setTransaksi(null);
                    oldObatOfTransaksi = em.merge(oldObatOfTransaksi);
                }
                transaksi.setObat(obat);
                transaksi = em.merge(transaksi);
            }
            if (supplier != null) {
                Obat oldObatOfSupplier = supplier.getObat();
                if (oldObatOfSupplier != null) {
                    oldObatOfSupplier.setSupplier(null);
                    oldObatOfSupplier = em.merge(oldObatOfSupplier);
                }
                supplier.setObat(obat);
                supplier = em.merge(supplier);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findObat(obat.getKodeObat()) != null) {
                throw new PreexistingEntityException("Obat " + obat + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Obat obat) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Obat persistentObat = em.find(Obat.class, obat.getKodeObat());
            Transaksi transaksiOld = persistentObat.getTransaksi();
            Transaksi transaksiNew = obat.getTransaksi();
            Supplier supplierOld = persistentObat.getSupplier();
            Supplier supplierNew = obat.getSupplier();
            List<String> illegalOrphanMessages = null;
            if (transaksiOld != null && !transaksiOld.equals(transaksiNew)) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("You must retain Transaksi " + transaksiOld + " since its obat field is not nullable.");
            }
            if (supplierOld != null && !supplierOld.equals(supplierNew)) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("You must retain Supplier " + supplierOld + " since its obat field is not nullable.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (transaksiNew != null) {
                transaksiNew = em.getReference(transaksiNew.getClass(), transaksiNew.getKodeTransaksi());
                obat.setTransaksi(transaksiNew);
            }
            if (supplierNew != null) {
                supplierNew = em.getReference(supplierNew.getClass(), supplierNew.getIdPerusahaan());
                obat.setSupplier(supplierNew);
            }
            obat = em.merge(obat);
            if (transaksiNew != null && !transaksiNew.equals(transaksiOld)) {
                Obat oldObatOfTransaksi = transaksiNew.getObat();
                if (oldObatOfTransaksi != null) {
                    oldObatOfTransaksi.setTransaksi(null);
                    oldObatOfTransaksi = em.merge(oldObatOfTransaksi);
                }
                transaksiNew.setObat(obat);
                transaksiNew = em.merge(transaksiNew);
            }
            if (supplierNew != null && !supplierNew.equals(supplierOld)) {
                Obat oldObatOfSupplier = supplierNew.getObat();
                if (oldObatOfSupplier != null) {
                    oldObatOfSupplier.setSupplier(null);
                    oldObatOfSupplier = em.merge(oldObatOfSupplier);
                }
                supplierNew.setObat(obat);
                supplierNew = em.merge(supplierNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = obat.getKodeObat();
                if (findObat(id) == null) {
                    throw new NonexistentEntityException("The obat with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Obat obat;
            try {
                obat = em.getReference(Obat.class, id);
                obat.getKodeObat();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The obat with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Transaksi transaksiOrphanCheck = obat.getTransaksi();
            if (transaksiOrphanCheck != null) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Obat (" + obat + ") cannot be destroyed since the Transaksi " + transaksiOrphanCheck + " in its transaksi field has a non-nullable obat field.");
            }
            Supplier supplierOrphanCheck = obat.getSupplier();
            if (supplierOrphanCheck != null) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Obat (" + obat + ") cannot be destroyed since the Supplier " + supplierOrphanCheck + " in its supplier field has a non-nullable obat field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(obat);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Obat> findObatEntities() {
        return findObatEntities(true, -1, -1);
    }

    public List<Obat> findObatEntities(int maxResults, int firstResult) {
        return findObatEntities(false, maxResults, firstResult);
    }

    private List<Obat> findObatEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Obat.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Obat findObat(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Obat.class, id);
        } finally {
            em.close();
        }
    }

    public int getObatCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Obat> rt = cq.from(Obat.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
