import type { FactCheck } from "./types/fact-check";

export const videoManifest: FactCheck[] = [
  {
    id: "claim-1",
    startSeconds: 10,
    endSeconds: 18,
    claim: "The United States has 52 states.",
    verdict: "FALSE",
    explanation:
      "The United States consists of 50 states. Washington, D.C. and U.S. territories are not states.",
    confidence: 0.99,
    sources: [
      {
        title: "State Governments",
        publisher: "USA.gov",
        url: "https://www.usa.gov/state-governments",
      },
      {
        title: "Frequently Asked Questions",
        publisher: "U.S. Census Bureau",
        url: "https://www.census.gov",
      },
    ],
  },
  {
    id: "claim-2",
    startSeconds: 30,
    endSeconds: 40,
    claim:
      "Water freezes at 0 degrees Celsius under standard atmospheric pressure.",
    verdict: "TRUE",
    explanation:
      "Under standard atmospheric pressure, pure water freezes at approximately 0 degrees Celsius.",
    confidence: 0.98,
    sources: [
      {
        title: "Water Properties",
        publisher: "United States Geological Survey",
        url: "https://www.usgs.gov/special-topics/water-science-school",
      },
      {
        title: "Water",
        publisher: "National Institute of Standards and Technology",
        url: "https://www.nist.gov",
      },
    ],
  },
  {
    id: "claim-3",
    startSeconds: 55,
    endSeconds: 68,
    claim: "Coffee is always harmful to human health.",
    verdict: "FALSE",
    explanation:
      "Research does not support the claim that coffee is always harmful. Moderate coffee consumption may be safe for many adults, although individual risks vary.",
    confidence: 0.9,
    sources: [
      {
        title: "Coffee and Health",
        publisher: "Harvard T.H. Chan School of Public Health",
        url: "https://www.hsph.harvard.edu/nutritionsource/food-features/coffee/",
      },
      {
        title: "Spilling the Beans: How Much Caffeine is Too Much?",
        publisher: "U.S. Food and Drug Administration",
        url: "https://www.fda.gov/consumers/consumer-updates/spilling-beans-how-much-caffeine-too-much",
      },
    ],
  },
  {
    id: "claim-4",
    startSeconds: 80,
    endSeconds: 94,
    claim: "Remote work always makes employees more productive.",
    verdict: "INCONCLUSIVE",
    explanation:
      "Research findings vary by job type, organization, employee circumstances, and how productivity is measured. The evidence does not support a universal conclusion.",
    confidence: 0.72,
    sources: [
      {
        title: "The Evolution of Working from Home",
        publisher: "Stanford Institute for Economic Policy Research",
        url: "https://siepr.stanford.edu",
      },
      {
        title: "Teleworking and Productivity",
        publisher: "Organisation for Economic Co-operation and Development",
        url: "https://www.oecd.org",
      },
    ],
  },
  {
    id: "claim-5",
    startSeconds: 110,
    endSeconds: 122,
    claim: "Humans use only 10 percent of their brains.",
    verdict: "FALSE",
    explanation:
      "Brain imaging and neurological research show that humans use many regions of the brain across normal daily activities. The 10-percent claim is a myth.",
    confidence: 0.98,
    sources: [
      {
        title: "Do We Really Use Only 10 Percent of Our Brain?",
        publisher: "Scientific American",
        url: "https://www.scientificamerican.com",
      },
      {
        title: "Brain Basics",
        publisher: "National Institute of Neurological Disorders and Stroke",
        url: "https://www.ninds.nih.gov",
      },
    ],
  },
];
