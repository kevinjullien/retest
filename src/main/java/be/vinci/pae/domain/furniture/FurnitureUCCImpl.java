package be.vinci.pae.domain.furniture;

import java.util.List;
import java.util.Map;

import be.vinci.pae.api.exceptions.BusinessException;
import be.vinci.pae.api.exceptions.UnauthorizedException;
import be.vinci.pae.domain.address.Address;
import be.vinci.pae.domain.furniture.FurnitureDTO.Condition;
import be.vinci.pae.services.dal.DalServices;
import be.vinci.pae.services.dao.FurnitureDAO;
import be.vinci.pae.services.dao.UserDAO;
import jakarta.inject.Inject;

public class FurnitureUCCImpl implements FurnitureUCC {

  @Inject
  private FurnitureDAO furnitureDao;

  @Inject
  private UserDAO userDAO;

  @Inject
  private DalServices dalServices;

  @Override
  public void indicateSentToWorkshop(int id) {
    dalServices.getConnection(false);
    Furniture furniture = (Furniture) furnitureDao.getFurnitureById(id);

    dalServices.commitTransactionAndContinue();

    if (furniture.getCondition().equals(Condition.ACHETE)) {
      furnitureDao.indicateSentToWorkshop(id);

      dalServices.commitTransaction();

    } else {
      dalServices.rollbackTransaction();
      throw new BusinessException("State error");
    }
  }

  @Override
  public void indicateDropOfStore(int id) {
    dalServices.getConnection(false);
    Furniture furniture = (Furniture) furnitureDao.getFurnitureById(id);

    dalServices.commitTransactionAndContinue();

    if (furniture.getCondition().equals(Condition.EN_RESTAURATION)
        || furniture.getCondition().equals(Condition.ACHETE)) {
      furnitureDao.indicateDropOfStore(id);
      dalServices.commitTransaction();

    } else {
      dalServices.rollbackTransaction();
      throw new BusinessException("State error");
    }
  }

  @Override
  public void indicateOfferedForSale(int id, double price) {
    dalServices.getConnection(false);
    Furniture furniture = (Furniture) furnitureDao.getFurnitureById(id);

    dalServices.commitTransactionAndContinue();

    if (price > 0 && furniture.getCondition().equals(Condition.DEPOSE_EN_MAGASIN)) {
      furnitureDao.indicateOfferedForSale(furniture, price);

      dalServices.commitTransaction();

    } else {
      dalServices.rollbackTransaction();
      throw new BusinessException("State error");
    }
  }

  @Override
  public void withdrawSale(int id) {
    dalServices.getConnection(false);
    Furniture furniture = (Furniture) furnitureDao.getFurnitureById(id);

    if (furniture.getCondition().equals(Condition.EN_VENTE)) {
      furnitureDao.withdrawSale(id);
      dalServices.commitTransaction();
    } else {
      dalServices.rollbackTransaction();
      throw new BusinessException("State error");
    }
  }

  @Override
  public void introduceOption(int optionTerm, int idUser, int idFurniture) {
    if (optionTerm <= 0) {
      throw new UnauthorizedException("optionTerm negative");
    }
    dalServices.getConnection(false);
    int nbrDaysActually = furnitureDao.getNumberOfReservation(idFurniture, idUser);

    dalServices.commitTransactionAndContinue();

    if (nbrDaysActually == 5) {
      dalServices.rollbackTransaction();
      throw new UnauthorizedException("You have already reached the maximum number of days");
    } else if (nbrDaysActually + optionTerm > 5) {
      dalServices.rollbackTransaction();
      int daysLeft = 5 - nbrDaysActually;
      throw new UnauthorizedException("You can't book more than : " + daysLeft + " days");
    } else {
      furnitureDao.introduceOption(optionTerm, idUser, idFurniture);
      dalServices.commitTransaction();
    }
  }

  @Override
  public void cancelOption(String cancellationReason, int idOption) {
    if (idOption < 1) {
      throw new BusinessException("Invalid id");
    }
    dalServices.getConnection(true);
    furnitureDao.cancelOption(cancellationReason, idOption);
    dalServices.commitTransaction();
  }

  @Override
  public List<FurnitureDTO> getFurnitureList() {
    dalServices.getConnection(false);
    List<FurnitureDTO> furnitureList = furnitureDao.getFurnitureList();
    dalServices.commitTransaction();
    return furnitureList;
  }


  @Override
  public void introduceRequestForVisite(String timeSlot, Address address,
      Map<Integer, List<String>> furnitures) {
    // TODO Auto-generated method stub
  }


  public FurnitureDTO getFurnitureById(int id) {
    dalServices.getConnection(false);
    FurnitureDTO furniture = furnitureDao.getFurnitureById(id);
    // dalServices.commitTransactionAndContinue();
    furniture.setSeller(userDAO.getUserFromId(furniture.getSellerId()));
    // TODO tests si présent?
    furniture.setType(furnitureDao.getTypeById(furniture.getTypeId()));
    furniture
        .setFavouritePhoto(furnitureDao.getFavouritePhotoById(furniture.getFavouritePhotoId()));
    // dalServices.commitTransaction();
    dalServices.commitTransaction();
    return furniture;
  }



}
