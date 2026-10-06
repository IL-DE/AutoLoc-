package tn.esprit.autoloc.service;

import tn.esprit.autoloc.domain.Paiement;

import java.util.List;

public interface IPaiementService {
    List<Paiement> retrieveAllPaiements();
    Paiement addPaiement(Paiement p);
    Paiement updatePaiement(Paiement p);
    Paiement retrievePaiement(Long idPaiement);
    void removePaiement(Long idPaiement);
}
