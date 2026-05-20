package com.bspapeleria.backend.service;

import com.bspapeleria.backend.dto.*;
import com.bspapeleria.backend.entity.Curso;
import com.bspapeleria.backend.entity.Leccion;
import com.bspapeleria.backend.entity.Modulo;
import com.bspapeleria.backend.exception.BadRequestException;
import com.bspapeleria.backend.exception.ResourceNotFoundException;
import com.bspapeleria.backend.repository.CursoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CursoService {

    private final CursoRepository cursoRepository;

    @Transactional(readOnly = true)
    public Page<CursoResponse> getAllCursos(Pageable pageable) {
        return cursoRepository.findByActivoTrue(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CursoResponse getCursoBySlug(String slug) {
        Curso curso = cursoRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado: " + slug));
        return toResponse(curso);
    }

    @Transactional(readOnly = true)
    public CursoResponse getCursoById(Long id) {
        Curso curso = cursoRepository.findByIdWithModulos(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado: " + id));
        return toResponse(curso);
    }

    @Transactional(readOnly = true)
    public Page<CursoResponse> getCursosByNivel(String nivel, Pageable pageable) {
        Curso.Nivel nivelEnum;
        try {
            nivelEnum = Curso.Nivel.valueOf(nivel.toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Nivel inválido: " + nivel);
        }
        return cursoRepository.findByNivelAndActivoTrue(nivelEnum, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CursoResponse> getCursosByModalidad(String modalidad, Pageable pageable) {
        Curso.Modalidad modEnum;
        try {
            modEnum = Curso.Modalidad.valueOf(modalidad.toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Modalidad inválida: " + modalidad);
        }
        return cursoRepository.findByModalidadAndActivoTrue(modEnum, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CursoResponse> searchCursos(String search, Pageable pageable) {
        String safeSearch = (search == null) ? "" : search;
        if (safeSearch.isEmpty()) {
            return cursoRepository.findByActivoTrue(pageable).map(this::toResponse);
        }
        return cursoRepository.searchCursos(safeSearch, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CursoResponse> getCursosByPrecioRange(Double minPrice, Double maxPrice, Pageable pageable) {
        return cursoRepository.findByPrecioRange(minPrice, maxPrice, pageable).map(this::toResponse);
    }

    @Transactional
    public CursoResponse createCurso(CursoRequest request) {
        Curso curso = Curso.builder()
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .imagenUrl(request.getImagenUrl())
                .precio(request.getPrecio())
                .precioComparacion(request.getPrecioComparacion())
                .nivel(parseNivel(request.getNivel()))
                .modalidad(parseModalidad(request.getModalidad()))
                .instructor(request.getInstructor())
                .duracionHoras(request.getDuracionHoras())
                .urlVideoIntro(request.getUrlVideoIntro())
                .tags(request.getTags() != null ? new java.util.HashSet<>(request.getTags()) : new java.util.HashSet<>())
                .materialUrls(request.getMaterialUrls() != null ? new ArrayList<>(request.getMaterialUrls()) : new ArrayList<>())
                .modulos(new HashSet<>())
                .build();

        if (request.getModulos() != null && !request.getModulos().isEmpty()) {
            Set<Modulo> modulos = buildModulos(request.getModulos(), curso);
            curso.setModulos(modulos);
        }

        cursoRepository.save(curso);
        return toResponse(curso);
    }

    @Transactional
    public CursoResponse updateCurso(Long id, CursoRequest request) {
        Curso curso = cursoRepository.findByIdWithModulos(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado: " + id));

        curso.setTitulo(request.getTitulo());
        curso.setDescripcion(request.getDescripcion());
        curso.setImagenUrl(request.getImagenUrl());
        curso.setPrecio(request.getPrecio());
        curso.setPrecioComparacion(request.getPrecioComparacion());
        curso.setNivel(parseNivel(request.getNivel()));
        curso.setModalidad(parseModalidad(request.getModalidad()));
        curso.setInstructor(request.getInstructor());
        curso.setDuracionHoras(request.getDuracionHoras());
        curso.setUrlVideoIntro(request.getUrlVideoIntro());
        curso.setTags(request.getTags() != null ? new java.util.HashSet<>(request.getTags()) : new java.util.HashSet<>());
        curso.setMaterialUrls(request.getMaterialUrls() != null ? new ArrayList<>(request.getMaterialUrls()) : new ArrayList<>());

        if (request.getModulos() != null) {
            curso.getModulos().clear();
            Set<Modulo> modulos = buildModulos(request.getModulos(), curso);
            curso.getModulos().addAll(modulos);
        }

        cursoRepository.save(curso);
        return toResponse(curso);
    }

    @Transactional
    public void deleteCurso(Long id) {
        if (!cursoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Curso no encontrado: " + id);
        }
        cursoRepository.deleteById(id);
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private Set<Modulo> buildModulos(List<ModuloRequest> moduloRequests, Curso curso) {
        return moduloRequests.stream()
                .map(mr -> {
                    Modulo modulo = Modulo.builder()
                            .curso(curso)
                            .titulo(mr.getTitulo())
                            .descripcion(mr.getDescripcion())
                            .orden(mr.getOrden() != null ? mr.getOrden() : 0)
                            .lecciones(new HashSet<>())
                            .build();

                    if (mr.getLecciones() != null && !mr.getLecciones().isEmpty()) {
                        Set<Leccion> lecciones = mr.getLecciones().stream()
                                .map(lr -> Leccion.builder()
                                        .modulo(modulo)
                                        .titulo(lr.getTitulo())
                                        .contenido(lr.getContenido())
                                        .urlVideo(lr.getUrlVideo())
                                        .urlMaterial(lr.getUrlMaterial())
                                        .orden(lr.getOrden() != null ? lr.getOrden() : 0)
                                        .duracionMinutos(lr.getDuracionMinutos() != null ? lr.getDuracionMinutos() : 0)
                                        .esPreview(lr.getEsPreview() != null ? lr.getEsPreview() : false)
                                        .build())
                                .collect(Collectors.toCollection(HashSet::new));
                        modulo.setLecciones(lecciones);
                    }

                    return modulo;
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Curso.Nivel parseNivel(String nivel) {
        if (nivel == null || nivel.isEmpty()) return Curso.Nivel.principiante;
        try {
            return Curso.Nivel.valueOf(nivel.toLowerCase());
        } catch (IllegalArgumentException e) {
            return Curso.Nivel.principiante;
        }
    }

    private Curso.Modalidad parseModalidad(String modalidad) {
        if (modalidad == null || modalidad.isEmpty()) return Curso.Modalidad.video;
        try {
            return Curso.Modalidad.valueOf(modalidad.toLowerCase());
        } catch (IllegalArgumentException e) {
            return Curso.Modalidad.video;
        }
    }

    private CursoResponse toResponse(Curso curso) {
        List<ModuloResponse> modulos = null;
        if (curso.getModulos() != null && !curso.getModulos().isEmpty()) {
            modulos = curso.getModulos().stream()
                    .map(m -> {
                        List<LeccionResponse> lecciones = m.getLecciones() == null ? new ArrayList<>() :
                                m.getLecciones().stream()
                                        .map(l -> LeccionResponse.builder()
                                                .id(l.getId())
                                                .titulo(l.getTitulo())
                                                .contenido(l.getContenido())
                                                .urlVideo(l.getUrlVideo())
                                                .urlMaterial(l.getUrlMaterial())
                                                .orden(l.getOrden())
                                                .duracionMinutos(l.getDuracionMinutos())
                                                .esPreview(l.getEsPreview())
                                                .build())
                                        .collect(Collectors.toList());

                        return ModuloResponse.builder()
                                .id(m.getId())
                                .titulo(m.getTitulo())
                                .descripcion(m.getDescripcion())
                                .orden(m.getOrden())
                                .lecciones(lecciones)
                                .build();
                    })
                    .collect(Collectors.toList());
        }

        return CursoResponse.builder()
                .id(curso.getId())
                .slug(curso.getSlug())
                .titulo(curso.getTitulo())
                .descripcion(curso.getDescripcion())
                .imagenUrl(curso.getImagenUrl())
                .precio(curso.getPrecio())
                .precioComparacion(curso.getPrecioComparacion())
                .nivel(curso.getNivel().name())
                .modalidad(curso.getModalidad().name())
                .instructor(curso.getInstructor())
                .duracionHoras(curso.getDuracionHoras())
                .urlVideoIntro(curso.getUrlVideoIntro())
                .activo(curso.getActivo())
                .estudiantesCount(curso.getEstudiantesCount())
                .rating(curso.getRating())
                .tags(curso.getTags() != null ? new ArrayList<>(curso.getTags()) : new ArrayList<>())
                .materialUrls(curso.getMaterialUrls() != null ? new ArrayList<>(curso.getMaterialUrls()) : new ArrayList<>())
                .modulos(modulos)
                .createdAt(curso.getFechaCreacion())
                .build();
    }
}
