export function formatMontantFCFA(montant) {
  const formatted = new Intl.NumberFormat('fr-FR').format(montant);
  return `${formatted} FCFA`;
}

export function formatDate(dateString) {
  const date = new Date(dateString);
  return new Intl.DateTimeFormat('fr-FR', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  }).format(date);
}

export function formatDateHeure(dateTimeString) {
  const date = new Date(dateTimeString);
  const datePart = formatDate(date);
  const heures = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  return `${datePart} à ${heures}h${minutes}`;
}

const MOIS_LABELS = [
  'Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin',
  'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre',
];

export function getPeriodeLabel(mois, annee) {
  return `${MOIS_LABELS[mois - 1]} ${annee}`;
}