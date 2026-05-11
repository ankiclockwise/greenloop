import { useEffect, useMemo, useState } from "react";
import axios from "axios";
import {
  IMPACT_API_ENDPOINTS,
  IMPACT_COPY,
  INITIAL_PAGE,
  PAGE_SIZE
} from "../constants/impactConstants";
import {
  BADGE_CATALOG,
  DONOR_LEADERBOARD,
  STUDENT_LEADERBOARD,
  createFallbackImpact
} from "../data/impactMockData";
import { createImpactMetrics, paginate } from "../utils/impactUtils";

function createLeaderboardState(items) {
  return {
    items: paginate(items, INITIAL_PAGE, PAGE_SIZE),
    total: items.length
  };
}

export function useImpactData(user) {
  const [impact, setImpact] = useState(() => createFallbackImpact(user));
  const [badges, setBadges] = useState(BADGE_CATALOG);
  const [donorLeaderboard, setDonorLeaderboard] = useState(() => createLeaderboardState(DONOR_LEADERBOARD));
  const [studentLeaderboard, setStudentLeaderboard] = useState(() => createLeaderboardState(STUDENT_LEADERBOARD));
  const [donorPage, setDonorPage] = useState(INITIAL_PAGE);
  const [studentPage, setStudentPage] = useState(INITIAL_PAGE);
  const [dataNote, setDataNote] = useState("");

  const visibleMetrics = useMemo(() => createImpactMetrics(impact), [impact]);

  useEffect(() => {
    let cancelled = false;

    async function loadImpact() {
      try {
        const [impactResponse, badgesResponse] = await Promise.all([
          axios.get(IMPACT_API_ENDPOINTS.ME),
          axios.get(IMPACT_API_ENDPOINTS.BADGES)
        ]);

        if (cancelled) return;

        setImpact({ ...createFallbackImpact(user), ...impactResponse.data });
        setBadges(badgesResponse.data?.badges || BADGE_CATALOG);
        setDataNote("");
      } catch {
        if (!cancelled) {
          setImpact(createFallbackImpact(user));
          setBadges(BADGE_CATALOG);
          setDataNote(IMPACT_COPY.DATA_NOTE);
        }
      }
    }

    loadImpact();

    return () => {
      cancelled = true;
    };
  }, [user]);

  useEffect(() => {
    let cancelled = false;

    async function loadDonors() {
      try {
        const response = await axios.get(IMPACT_API_ENDPOINTS.DONOR_LEADERBOARD, {
          params: { page: donorPage, limit: PAGE_SIZE }
        });

        if (!cancelled) {
          setDonorLeaderboard({
            items: response.data?.items || [],
            total: response.data?.total || 0
          });
        }
      } catch {
        if (!cancelled) {
          setDonorLeaderboard({
            items: paginate(DONOR_LEADERBOARD, donorPage, PAGE_SIZE),
            total: DONOR_LEADERBOARD.length
          });
        }
      }
    }

    loadDonors();

    return () => {
      cancelled = true;
    };
  }, [donorPage]);

  useEffect(() => {
    let cancelled = false;

    async function loadStudents() {
      try {
        const response = await axios.get(IMPACT_API_ENDPOINTS.STUDENT_LEADERBOARD, {
          params: { page: studentPage, limit: PAGE_SIZE }
        });

        if (!cancelled) {
          setStudentLeaderboard({
            items: response.data?.items || [],
            total: response.data?.total || 0
          });
        }
      } catch {
        if (!cancelled) {
          setStudentLeaderboard({
            items: paginate(STUDENT_LEADERBOARD, studentPage, PAGE_SIZE),
            total: STUDENT_LEADERBOARD.length
          });
        }
      }
    }

    loadStudents();

    return () => {
      cancelled = true;
    };
  }, [studentPage]);

  return {
    badges,
    dataNote,
    donorLeaderboard,
    donorPage,
    impact,
    setDonorPage,
    setStudentPage,
    studentLeaderboard,
    studentPage,
    visibleMetrics
  };
}
