package tn.esprit.autoloc.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.repository.IVehiculeRepository;
import tn.esprit.autoloc.service.IVehiculeService;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class VehiculeServiceImpl implements IVehiculeService {

    private final IVehiculeRepository vehiculeRepository;

    @Override
    public List<Vehicule> retrieveAllVehicules() {
        log.info("Retrieving all vehicules");
        return vehiculeRepository.findAll();
    }

    @Override
    public Vehicule addVehicule(Vehicule v) {
        log.info("Adding vehicule: {}", v.getImmatriculation());
        return vehiculeRepository.save(v);
    }

    @Override
    public Vehicule updateVehicule(Vehicule v) {
        log.info("Updating vehicule id: {}", v.getIdVehicule());
        return vehiculeRepository.save(v);
    }

    @Override
    public Vehicule retrieveVehicule(Long idVehicule) {
        log.info("Retrieving vehicule id: {}", idVehicule);
        return vehiculeRepository.findById(idVehicule)
                .orElse(null);
    }

    @Override
    public void removeVehicule(Long idVehicule) {
        log.info("Removing vehicule id: {}", idVehicule);
        vehiculeRepository.deleteById(idVehicule);
    }

    @Override
    public List<Vehicule> addVehicules(List<Vehicule> vehicules) {
        log.info("Adding {} vehicules", vehicules.size());
        return vehiculeRepository.saveAll(vehicules);
    }
}
