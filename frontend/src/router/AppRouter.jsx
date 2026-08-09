import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import ProtectedRoute from './ProtectedRoute';
import { AppLayout } from '../components/layout/AppLayout';
import Login from '../pages/auth/Login';
import CallbackKeycloak from '../pages/auth/CallbackKeycloak';
import AccesInterdit from '../pages/auth/AccesInterdit';
import VerifierMatriculePage from '../pages/enrolement/VerifierMatriculePage';
import ConfirmerEnrolementPage from '../pages/enrolement/ConfirmerEnrolementPage';
import ImporterBeneficiairesPage from '../pages/enrolement/ImporterBeneficiairesPage';
import Beneficiaires from '../pages/dashboard/Beneficiaires';
import GrillesListPage from '../pages/grilles/GrillesListPage';
import CreerGrillePage from '../pages/grilles/CreerGrillePage';
import HistoriqueGrillePage from '../pages/grilles/HistoriqueGrillePage';
import GrillesTarifairesValider from '../pages/dashboard/GrillesTarifairesValider';
import DashboardPage from '../pages/reporting/DashboardPage';
import HistoriquePage from '../pages/reporting/HistoriquePage';
import AuditPage from '../pages/reporting/AuditPage';
import ProcessusListPage from '../pages/workflow/ProcessusListPage';
import ProcessusDetailPage from '../pages/workflow/ProcessusDetailPage';
import DeclencherProcessusPage from '../pages/workflow/DeclencherProcessusPage';
import Admin from '../pages/admin/Admin';
import UtilisateursListPage from '../pages/admin/UtilisateursListPage';
import CreerUtilisateurPage from '../pages/admin/CreerUtilisateurPage';
import FonctionsEligiblesListPage from '../pages/admin/FonctionsEligiblesListPage';
import CreerFonctionPage from '../pages/admin/CreerFonctionPage';

const TOUS_ROLES = ['EMPLOYE', 'ARH', 'CRH', 'DRH', 'ADMIN'];

export default function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public */}
        <Route path="/login" element={<Login />} />
        <Route path="/auth/callback" element={<CallbackKeycloak />} />

        {/* Toute page ci-dessous exige une authentification et affiche le layout (sidebar) */}
        <Route element={<ProtectedRoute roles={TOUS_ROLES} />}>
          <Route element={<AppLayout />}>
            <Route path="/acces-interdit" element={<AccesInterdit />} />

            {/* EMPLOYE */}
            <Route element={<ProtectedRoute roles={['EMPLOYE']} />}>
              <Route path="/enrolement" element={<VerifierMatriculePage />} />
              <Route path="/enrolement/confirmer" element={<ConfirmerEnrolementPage />} />
            </Route>

            {/* Processus partage ARH, CRH, DRH */}
            <Route element={<ProtectedRoute roles={['ARH', 'CRH', 'DRH']} />}>
              <Route path="/processus" element={<ProcessusListPage />} />
              <Route path="/processus/:id" element={<ProcessusDetailPage />} />
            </Route>

            {/* Dashboard : ARH, DRH uniquement (CLAUDE.md section 8 groupe 6 --
                pas CRH, contrairement au guide de sprint 6F.8 ; decision actee
                le 2026-07-30 : CLAUDE.md fait foi). */}
            <Route element={<ProtectedRoute roles={['ARH', 'DRH']} />}>
              <Route path="/dashboard" element={<DashboardPage />} />
            </Route>

            {/* ARH (grilles tarifaires aussi consultables par ADMIN) */}
            <Route element={<ProtectedRoute roles={['ARH']} />}>
              <Route path="/beneficiaires" element={<Beneficiaires />} />
              <Route path="/beneficiaires/import" element={<ImporterBeneficiairesPage />} />
              {/* Declenchement reserve a l'ARH (POST /processus/declencher).
                  Route statique declaree avant /processus/:id -- React Router
                  privilegie de toute facon le segment statique. */}
              <Route path="/processus/declencher" element={<DeclencherProcessusPage />} />
            </Route>
            <Route element={<ProtectedRoute roles={['ARH', 'ADMIN']} />}>
              <Route path="/grilles-tarifaires" element={<GrillesListPage />} />
            </Route>
            <Route element={<ProtectedRoute roles={['ARH']} />}>
              <Route path="/grilles-tarifaires/creer" element={<CreerGrillePage />} />
            </Route>
            {/* Historique consultable par ARH, DRH, ADMIN (GET /grilles-tarifaires/fonction/{code}) */}
            <Route element={<ProtectedRoute roles={['ARH', 'DRH', 'ADMIN']} />}>
              <Route path="/grilles-tarifaires/historique/:code" element={<HistoriqueGrillePage />} />
            </Route>

            {/* Validation des grilles : CRH et DRH partagent l'ecran, chacun
                voyant son propre etage (Sprint MM.12). Roles STRICTEMENT
                identiques a ceux de l'entree NAV_LINKS correspondante dans
                Sidebar.jsx -- rappel de l'audit 6F.9. */}
            <Route element={<ProtectedRoute roles={['CRH', 'DRH']} />}>
              <Route path="/grilles-tarifaires/valider" element={<GrillesTarifairesValider />} />
            </Route>

            {/* DRH */}
            <Route element={<ProtectedRoute roles={['DRH']} />}>
              <Route path="/reporting/historique" element={<HistoriquePage />} />
              <Route path="/reporting/audit" element={<AuditPage />} />
            </Route>

            {/* ADMIN */}
            <Route element={<ProtectedRoute roles={['ADMIN']} />}>
              <Route path="/admin/utilisateurs" element={<UtilisateursListPage />} />
              <Route path="/admin/utilisateurs/creer" element={<CreerUtilisateurPage />} />
              <Route path="/admin/fonctions-eligibles" element={<FonctionsEligiblesListPage />} />
              <Route path="/admin/fonctions-eligibles/creer" element={<CreerFonctionPage />} />
              <Route path="/admin/*" element={<Admin />} />
            </Route>
          </Route>
        </Route>

        {/* Fallback */}
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}