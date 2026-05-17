package com.bspapeleria.backend.service;

import com.bspapeleria.backend.dto.ProgresoRequest;
import com.bspapeleria.backend.dto.ProgresoResponse;
import com.bspapeleria.backend.entity.Curso;
import com.bspapeleria.backend.entity.Progreso;
import com.bspapeleria.backend.entity.Usuario;
import com.bspapeleria.backend.exception.BadRequestException;
import com.bspapeleria.backend.exception.ResourceNotFoundException;
import com.bspapeleria.backend.repository.CursoRepository;
import com.bspapeleria.backend.repository.ProgresoRepository;
import com.bspapeleria.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgresoService {

    private final ProgresoRepository progresoRepository;
    private final CursoRepository cursoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<ProgresoResponse> getProgresosByUsuario(Long usuarioId) {
        return progresoRepository.findByUsuarioId(usuarioId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProgresoResponse getProgresoByUsuarioAndCurso(Long usuarioId, Long cursoId) {
        Progreso progreso = progresoRepository.findByUsuarioIdAndCursoId(usuarioId, cursoId)
                .orElseThrow(() -> new ResourceNotFoundException("Progreso no encontrado para este curso"));
        return toResponse(progreso);
    }

    @Transactional
    public ProgresoResponse inscribirUsuario(Long usuarioId, Long cursoId) {
        log.info("inscribirUsuario: usuarioId={}, cursoId={}", usuarioId, cursoId);
        if (progresoRepository.existsByUsuarioIdAndCursoId(usuarioId, cursoId)) {
            log.info("inscribirUsuario: Progreso ya existe para usuario {} y curso {}", usuarioId, cursoId);
            return toResponse(progresoRepository.findByUsuarioIdAndCursoId(usuarioId, cursoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Progreso no encontrado")));
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + usuarioId));
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado: " + cursoId));

        Progreso progreso = Progreso.builder()
                .usuario(usuario)
                .curso(curso)
                .leccionesCompletadas(new java.util.ArrayList<>())
                .porcentajeProgreso(0)
                .completado(false)
                .certificadoDesbloqueado(false)
                .build();

        progresoRepository.save(progreso);
        return toResponse(progreso);
    }

    @Transactional
    public ProgresoResponse marcarLeccionCompletada(Long usuarioId, Long cursoId, Long leccionId) {
        Progreso progreso = progresoRepository.findByUsuarioIdAndCursoId(usuarioId, cursoId)
                .orElseThrow(() -> new ResourceNotFoundException("Progreso no encontrado para este curso"));

        if (!progreso.getLeccionesCompletadas().contains(leccionId)) {
            progreso.getLeccionesCompletadas().add(leccionId);
        }

        int totalLecciones = cursoRepository.findById(cursoId)
                .map(c -> c.getLecciones().size())
                .orElse(0);

        if (totalLecciones > 0) {
            int porcentaje = (progreso.getLeccionesCompletadas().size() * 100) / totalLecciones;
            progreso.setPorcentajeProgreso(porcentaje);

            if (porcentaje >= 80 && !progreso.getCompletado()) {
                progreso.setCompletado(true);
                progreso.setFechaCompletado(LocalDateTime.now());
                progreso.setCertificadoDesbloqueado(true);
            }
        }

        progreso.setUltimaActividad(LocalDateTime.now());
        progresoRepository.save(progreso);
        return toResponse(progreso);
    }

    @Transactional
    public ProgresoResponse updateProgreso(Long usuarioId, Long cursoId, ProgresoRequest request) {
        Progreso progreso = progresoRepository.findByUsuarioIdAndCursoId(usuarioId, cursoId)
                .orElseThrow(() -> new ResourceNotFoundException("Progreso no encontrado para este curso"));

        if (request.getLeccionesCompletadas() != null) {
            progreso.setLeccionesCompletadas(request.getLeccionesCompletadas());
        }
        if (request.getLeccionActualId() != null) {
            progreso.setLeccionActualId(request.getLeccionActualId());
        }

        int totalLecciones = cursoRepository.findById(cursoId)
                .map(c -> c.getLecciones().size())
                .orElse(0);

        if (totalLecciones > 0) {
            int porcentaje = (progreso.getLeccionesCompletadas().size() * 100) / totalLecciones;
            progreso.setPorcentajeProgreso(porcentaje);

            if (porcentaje >= 80 && !progreso.getCompletado()) {
                progreso.setCompletado(true);
                progreso.setFechaCompletado(LocalDateTime.now());
                progreso.setCertificadoDesbloqueado(true);
            }
        }

        progreso.setUltimaActividad(LocalDateTime.now());
        progresoRepository.save(progreso);
        return toResponse(progreso);
    }

    private ProgresoResponse toResponse(Progreso progreso) {
        return ProgresoResponse.builder()
                .id(progreso.getId())
                .cursoId(progreso.getCurso().getId())
                .tituloCurso(progreso.getCurso().getTitulo())
                .leccionesCompletadas(progreso.getLeccionesCompletadas())
                .porcentajeProgreso(progreso.getPorcentajeProgreso())
                .leccionActualId(progreso.getLeccionActualId())
                .ultimaActividad(progreso.getUltimaActividad())
                .fechaInscripcion(progreso.getFechaInscripcion())
                .completado(progreso.getCompletado())
                .fechaCompletado(progreso.getFechaCompletado())
                .certificadoDesbloqueado(progreso.getCertificadoDesbloqueado())
                .build();
    }
}